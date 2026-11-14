package com.jovine360.talenthub.identity.domain.repository;

import com.jovine360.talenthub.identity.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);

    @org.springframework.data.jpa.repository.Query(
            "SELECT DISTINCT u FROM User u LEFT JOIN u.additionalRoles extra "
            + "WHERE (:role IS NULL OR u.role = :role OR extra = :role) "
            + "AND (:status IS NULL OR u.status = :status)")
    org.springframework.data.domain.Page<User> search(
            @org.springframework.data.repository.query.Param("role") com.jovine360.talenthub.identity.domain.model.UserRole role,
            @org.springframework.data.repository.query.Param("status") com.jovine360.talenthub.identity.domain.model.UserStatus status,
            org.springframework.data.domain.Pageable pageable);
}
