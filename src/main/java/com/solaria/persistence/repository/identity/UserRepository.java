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

    String PEOPLE = "SELECT u.id, (SELECT min(p.name) FROM person p WHERE p.user_id=u.id) AS name, "
            + "u.username, u.avatar, u.banner FROM user_account u WHERE u.is_active=true AND u.id<>:owner "
            + "AND EXISTS (SELECT 1 FROM person p WHERE p.user_id=u.id) "
            + "AND (CAST(:company AS uuid) IS NULL OR u.managed_by_company_id=:company "
            + "OR EXISTS (SELECT 1 FROM user_company m WHERE m.user_id=u.id AND m.company_id=:company)) "
            + "AND (u.managed_by_company_id IS NULL OR u.managed_by_company_id=:company "
            + "OR EXISTS (SELECT 1 FROM user_company m WHERE m.user_id=:owner AND m.company_id=u.managed_by_company_id)) ";
    String SEARCH = "AND (lower(u.username) LIKE concat('%',CAST(:query AS text),'%') OR EXISTS "
            + "(SELECT 1 FROM person p WHERE p.user_id=u.id AND lower(p.name) LIKE concat('%',CAST(:query AS text),'%'))) ";

    @Query(value = PEOPLE + SEARCH + "ORDER BY lower(u.username), u.id", nativeQuery = true)
    org.springframework.data.domain.Slice<ConnectionPerson> findConnectionPeople(
            @Param("owner") UUID owner, @Param("company") UUID company,
            @Param("query") String query, org.springframework.data.domain.Pageable pageable);

    @Query(value = PEOPLE + SEARCH
            + "AND u.id=ANY(SELECT unnest(o.connection_user_ids) FROM user_account o WHERE o.id=:owner) "
            + "ORDER BY lower(u.username), u.id", nativeQuery = true)
    org.springframework.data.domain.Slice<ConnectionPerson> findConnectedPeople(
            @Param("owner") UUID owner, @Param("company") UUID company,
            @Param("query") String query, org.springframework.data.domain.Pageable pageable);

    @Query(value = PEOPLE + "AND u.id=:target", nativeQuery = true)
    Optional<ConnectionPerson> findVisiblePerson(@Param("owner") UUID owner,
            @Param("company") UUID company, @Param("target") UUID target);

    @Query("select u from User u where u.authId = :authId")
    Optional<User> findByAuthId(@Param("authId") UUID authId);

    @Query("select u.id from User u where u.authId = :authId")
    Optional<UUID> findIdByAuthId(@Param("authId") UUID authId);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Query(value = "UPDATE user_account SET connection_user_ids = array_remove(connection_user_ids, :userId) "
            + "WHERE :userId = ANY(connection_user_ids)", nativeQuery = true)
    void removeConnectionReferences(@Param("userId") UUID userId);

}
