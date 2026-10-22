package com.solaria.persistence.repository.identity;

import com.solaria.persistence.domain.entity.identity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    interface ConnectionPerson {
        UUID getId();
        String getName();
        String getUsername();
        String getAvatar();
        String getBanner();
    }

    String PEOPLE = "SELECT u.id, (SELECT min(p.name) FROM person p WHERE p.fk_users=u.id) AS name, "
            + "u.username, u.avatar, u.banner FROM users u WHERE u.active=true AND u.id<>:owner "
            + "AND EXISTS (SELECT 1 FROM person p WHERE p.fk_users=u.id) "
            + "AND (CAST(:company AS uuid) IS NULL OR u.managed_by_company_id=:company "
            + "OR EXISTS (SELECT 1 FROM user_company m WHERE m.fk_users=u.id AND m.fk_company=:company)) "
            + "AND (u.managed_by_company_id IS NULL OR u.managed_by_company_id=:company "
            + "OR EXISTS (SELECT 1 FROM user_company m WHERE m.fk_users=:owner AND m.fk_company=u.managed_by_company_id)) ";
    String SEARCH = "AND (lower(u.username) LIKE concat('%',CAST(:query AS text),'%') OR EXISTS "
            + "(SELECT 1 FROM person p WHERE p.fk_users=u.id AND lower(p.name) LIKE concat('%',CAST(:query AS text),'%'))) ";

    @Query(value = PEOPLE + SEARCH + "ORDER BY lower(u.username), u.id", nativeQuery = true)
    org.springframework.data.domain.Slice<ConnectionPerson> findConnectionPeople(
            @Param("owner") UUID owner, @Param("company") UUID company,
            @Param("query") String query, org.springframework.data.domain.Pageable pageable);

    @Query(value = PEOPLE + SEARCH
            + "AND u.id=ANY(SELECT unnest(o.connections) FROM users o WHERE o.id=:owner) "
            + "ORDER BY lower(u.username), u.id", nativeQuery = true)
    org.springframework.data.domain.Slice<ConnectionPerson> findConnectedPeople(
            @Param("owner") UUID owner, @Param("company") UUID company,
            @Param("query") String query, org.springframework.data.domain.Pageable pageable);

    @Query(value = PEOPLE + "AND u.id=:target", nativeQuery = true)
    Optional<ConnectionPerson> findVisiblePerson(@Param("owner") UUID owner,
            @Param("company") UUID company, @Param("target") UUID target);

    @Query("select u from User u where u.auth_id = :authId")
    Optional<User> findByAuthId(@Param("authId") UUID authId);

    @Query("select u.id from User u where u.auth_id = :authId")
    Optional<UUID> findIdByAuthId(@Param("authId") UUID authId);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Query(value = "UPDATE users SET connections = array_remove(connections, :userId) "
            + "WHERE :userId = ANY(connections)", nativeQuery = true)
    void removeConnectionReferences(@Param("userId") UUID userId);

}
