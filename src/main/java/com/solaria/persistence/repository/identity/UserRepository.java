package com.solaria.persistence.repository.identity;

import com.solaria.persistence.domain.entity.identity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("select u from User u where u.auth_id = :authId")
    Optional<User> findByAuthId(@Param("authId") UUID authId);

    @Modifying
    @Query(value = "CALL sp_deactivate_inactive_users(:inactiveDays)", nativeQuery = true)
    void callDeactivateInactiveUsers(@Param("inactiveDays") int inactiveDays);
}
