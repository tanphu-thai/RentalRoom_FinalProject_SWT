package com.rrms.service;

import com.rrms.common.BusinessException;
import com.rrms.domain.entity.PasswordResetToken;
import com.rrms.domain.entity.UserAccount;
import com.rrms.domain.enums.UserRole;
import com.rrms.dto.AuthDtos;
import com.rrms.repository.PasswordResetTokenRepository;
import com.rrms.repository.UserAccountRepository;
import com.rrms.security.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserAccountRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    private Clock clock;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-06-23T00:00:00Z"), ZoneOffset.UTC);
        authService = new AuthService(userRepository, tokenRepository, new BCryptPasswordEncoder(), new SessionService(), clock, true);
    }

    @Test
    void resetPassword_otpAtExactExpiry_rejectAsExpired() {
        UserAccount user = user();
        PasswordResetToken token = token(user, LocalDateTime.now(clock));
        when(userRepository.findByEmailIgnoreCase("admin@rrms.local")).thenReturn(Optional.of(user));
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "123456")).thenReturn(Optional.of(token));
        AuthDtos.ResetPasswordRequest request = new AuthDtos.ResetPasswordRequest("admin@rrms.local", "123456", "NewPass1", "NewPass1");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.resetPassword(request));

        assertEquals("The verification code has expired. Please request a new one.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_validOtpBeforeExpiry_changePasswordAndInvalidateOtp() {
        UserAccount user = user();
        PasswordResetToken token = token(user, LocalDateTime.now(clock).plusSeconds(1));
        when(userRepository.findByEmailIgnoreCase("admin@rrms.local")).thenReturn(Optional.of(user));
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "123456")).thenReturn(Optional.of(token));
        AuthDtos.ResetPasswordRequest request = new AuthDtos.ResetPasswordRequest("admin@rrms.local", "123456", "NewPass1", "NewPass1");

        authService.resetPassword(request);

        assertTrue(token.isUsed());
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    private UserAccount user() {
        UserAccount user = new UserAccount(); user.setId(1L); user.setEmail("admin@rrms.local"); user.setUsername("admin"); user.setRole(UserRole.ADMIN); user.setActive(true); user.setPasswordHash("old"); return user;
    }
    private PasswordResetToken token(UserAccount user, LocalDateTime expiresAt) {
        PasswordResetToken token = new PasswordResetToken(); token.setUser(user); token.setCode("123456"); token.setExpiresAt(expiresAt); token.setUsed(false); return token;
    }
}
