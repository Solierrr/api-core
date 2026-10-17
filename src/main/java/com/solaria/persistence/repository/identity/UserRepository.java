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
