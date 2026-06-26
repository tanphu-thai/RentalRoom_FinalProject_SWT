package com.rrms.repository;

import com.rrms.domain.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findTopByUserIdAndCodeOrderByExpiresAtDesc(Long userId, String code);
}
