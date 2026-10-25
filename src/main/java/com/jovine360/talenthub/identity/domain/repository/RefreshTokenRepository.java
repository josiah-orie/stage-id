package com.jovine360.talenthub.identity.domain.repository;

import com.jovine360.talenthub.identity.domain.model.RefreshToken;
import com.jovine360.talenthub.identity.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    List<RefreshToken> findByUserAndRevokedFalse(User user);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllByUser(User user);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true OR rt.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredAndRevoked();
}
