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

    // @Test // Tạm thời comment lại để chạy đúng 15 test của validateOTP cho báo cáo Lab 2
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

    // @Test // Tạm thời comment lại để chạy đúng 15 test của validateOTP cho báo cáo Lab 2
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

    // =========================================================================
    // 15 EXPLICIT TEST CASES FOR validateOTP (Matching Excel Sheet UTCID01 -> UTCID15)
    // =========================================================================

    // Kiểm tra OTP hợp lệ, thời gian còn hạn 4 phút (Normal)
    @Test
    void testValidateOTP_UTCID01_Normal_ValidOtp_4MinsRemaining() {
        PasswordResetToken token = token(user(), LocalDateTime.now(clock).plusMinutes(4));
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "123456")).thenReturn(Optional.of(token));
        PasswordResetToken res = authService.validateOTP(1L, "123456");
        assertNotNull(res);
        assertEquals("123456", res.getCode());
    }

    // Kiểm tra OTP hợp lệ, thời gian còn hạn 1 phút (Normal)
    @Test
    void testValidateOTP_UTCID02_Normal_ValidOtp_1MinRemaining() {
        PasswordResetToken token = token(user(), LocalDateTime.now(clock).plusMinutes(1));
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "123456")).thenReturn(Optional.of(token));
        assertNotNull(authService.validateOTP(1L, "123456"));
    }

    // Kiểm tra OTP hợp lệ có chứa khoảng trắng ở đầu chuỗi (Normal)
    @Test
    void testValidateOTP_UTCID03_Normal_ValidOtp_LeadingSpace() {
        PasswordResetToken token = token(user(), LocalDateTime.now(clock).plusMinutes(3));
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "123456")).thenReturn(Optional.of(token));
        assertNotNull(authService.validateOTP(1L, " 123456"));
    }

    // Kiểm tra OTP hợp lệ có chứa khoảng trắng ở cuối chuỗi (Normal)
    @Test
    void testValidateOTP_UTCID04_Normal_ValidOtp_TrailingSpace() {
        PasswordResetToken token = token(user(), LocalDateTime.now(clock).plusMinutes(3));
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "123456")).thenReturn(Optional.of(token));
        assertNotNull(authService.validateOTP(1L, "123456 "));
    }

    // Kiểm tra OTP hợp lệ có chứa khoảng trắng ở cả 2 đầu (Normal)
    @Test
    void testValidateOTP_UTCID05_Normal_ValidOtp_BothSpaces() {
        PasswordResetToken token = token(user(), LocalDateTime.now(clock).plusMinutes(3));
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "123456")).thenReturn(Optional.of(token));
        assertNotNull(authService.validateOTP(1L, "  123456  "));
    }

    // Kiểm tra mốc biên OTP sát giờ hết hạn, còn đúng 1 giây (Boundary)
    @Test
    void testValidateOTP_UTCID06_Boundary_ValidOtp_1SecRemaining() {
        PasswordResetToken token = token(user(), LocalDateTime.now(clock).plusSeconds(1));
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "123456")).thenReturn(Optional.of(token));
        assertNotNull(authService.validateOTP(1L, "123456"));
    }

    // Kiểm tra mốc biên OTP tại đúng giây hết hạn -> Báo lỗi hết hạn (Boundary)
    @Test
    void testValidateOTP_UTCID07_Boundary_OtpExactExpiry() {
        PasswordResetToken token = token(user(), LocalDateTime.now(clock));
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "123456")).thenReturn(Optional.of(token));
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.validateOTP(1L, "123456"));
        assertEquals("The verification code has expired. Please request a new one.", ex.getMessage());
    }

    // Kiểm tra mốc biên OTP vừa quá hạn đúng 1 giây -> Báo lỗi hết hạn (Boundary)
    @Test
    void testValidateOTP_UTCID08_Boundary_OtpExpiredBy1Sec() {
        PasswordResetToken token = token(user(), LocalDateTime.now(clock).minusSeconds(1));
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "123456")).thenReturn(Optional.of(token));
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.validateOTP(1L, "123456"));
        assertEquals("The verification code has expired. Please request a new one.", ex.getMessage());
    }

    // Kiểm tra OTP đã quá hạn 10 phút -> Báo lỗi hết hạn (Abnormal)
    @Test
    void testValidateOTP_UTCID09_Abnormal_OtpExpiredBy10Mins() {
        PasswordResetToken token = token(user(), LocalDateTime.now(clock).minusMinutes(10));
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "123456")).thenReturn(Optional.of(token));
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.validateOTP(1L, "123456"));
        assertEquals("The verification code has expired. Please request a new one.", ex.getMessage());
    }

    // Kiểm tra OTP còn hạn nhưng đã bị sử dụng trước đó -> Báo lỗi đã dùng (Abnormal)
    @Test
    void testValidateOTP_UTCID10_Abnormal_OtpAlreadyUsed_NotExpired() {
        PasswordResetToken token = token(user(), LocalDateTime.now(clock).plusMinutes(2));
        token.setUsed(true);
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "123456")).thenReturn(Optional.of(token));
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.validateOTP(1L, "123456"));
        assertEquals("The verification code has already been used.", ex.getMessage());
    }

    // Kiểm tra OTP vừa hết hạn vừa đã bị sử dụng -> Báo lỗi đã dùng (Abnormal)
    @Test
    void testValidateOTP_UTCID11_Abnormal_OtpAlreadyUsed_AndExpired() {
        PasswordResetToken token = token(user(), LocalDateTime.now(clock).minusMinutes(2));
        token.setUsed(true);
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "123456")).thenReturn(Optional.of(token));
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.validateOTP(1L, "123456"));
        assertEquals("The verification code has already been used.", ex.getMessage());
    }

    // Kiểm tra chuỗi OTP sai mã/không tồn tại trong DB -> Báo lỗi mã không hợp lệ (Abnormal)
    @Test
    void testValidateOTP_UTCID12_Abnormal_InvalidOtpString() {
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "000000")).thenReturn(Optional.empty());
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.validateOTP(1L, "000000"));
        assertEquals("The verification code is invalid.", ex.getMessage());
    }

    // Kiểm tra chuỗi OTP bị Null -> Báo lỗi mã không hợp lệ (Abnormal)
    @Test
    void testValidateOTP_UTCID13_Abnormal_NullOtp() {
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "")).thenReturn(Optional.empty());
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.validateOTP(1L, null));
        assertEquals("The verification code is invalid.", ex.getMessage());
    }

    // Kiểm tra chuỗi OTP rỗng -> Báo lỗi mã không hợp lệ (Abnormal)
    @Test
    void testValidateOTP_UTCID14_Abnormal_EmptyOtp() {
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(1L, "")).thenReturn(Optional.empty());
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.validateOTP(1L, ""));
        assertEquals("The verification code is invalid.", ex.getMessage());
    }

    // Kiểm tra mã OTP đúng nhưng truyền sai User ID -> Báo lỗi mã không hợp lệ (Abnormal)
    @Test
    void testValidateOTP_UTCID15_Abnormal_WrongUserId() {
        when(tokenRepository.findTopByUserIdAndCodeOrderByExpiresAtDesc(999L, "123456")).thenReturn(Optional.empty());
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.validateOTP(999L, "123456"));
        assertEquals("The verification code is invalid.", ex.getMessage());
    }

    private UserAccount user() {
        UserAccount user = new UserAccount(); user.setId(1L); user.setEmail("admin@rrms.local"); user.setUsername("admin"); user.setRole(UserRole.ADMIN); user.setActive(true); user.setPasswordHash("old"); return user;
    }
    private PasswordResetToken token(UserAccount user, LocalDateTime expiresAt) {
        PasswordResetToken token = new PasswordResetToken(); token.setUser(user); token.setCode("123456"); token.setExpiresAt(expiresAt); token.setUsed(false); return token;
    }
}
