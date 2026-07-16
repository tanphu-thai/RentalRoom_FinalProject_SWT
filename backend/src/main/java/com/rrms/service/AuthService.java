package com.rrms.service;

import com.rrms.common.BusinessException;
import com.rrms.domain.entity.PasswordResetToken;
import com.rrms.domain.entity.UserAccount;
import com.rrms.domain.enums.UserRole;
import com.rrms.dto.AuthDtos;
import com.rrms.repository.PasswordResetTokenRepository;
import com.rrms.repository.UserAccountRepository;
import com.rrms.security.SessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern STRONG_PASSWORD = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private final UserAccountRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final Clock clock;
    private final boolean exposeOtp;

    public AuthService(UserAccountRepository userRepository,
                       PasswordResetTokenRepository resetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       SessionService sessionService,
                       Clock clock,
                       @Value("${app.demo.expose-otp:true}") boolean exposeOtp) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
        this.clock = clock;
        this.exposeOtp = exposeOtp;
    }

    @Transactional
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest request) {
        String input = request.username().trim();
        UserAccount user = userRepository.findByUsernameIgnoreCase(input)
                .or(() -> userRepository.findByEmailIgnoreCase(input))
                .orElseThrow(() -> BusinessException.unauthorized("Invalid username or password."));

        LocalDateTime now = LocalDateTime.now(clock);
        if (!user.isActive()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Your account has been deactivated. Please contact the Administrator.");
        }
        if (user.getLockedUntil() != null && now.isBefore(user.getLockedUntil())) {
            throw BusinessException.unauthorized("Your account is temporarily locked due to multiple failed login attempts.");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            int failed = user.getFailedLoginCount() + 1;
            user.setFailedLoginCount(failed);
            if (failed >= 5) {
                user.setLockedUntil(now.plusMinutes(15));
                userRepository.save(user);
                throw BusinessException.unauthorized("Your account is temporarily locked due to multiple failed login attempts.");
            }
            userRepository.save(user);
            throw BusinessException.unauthorized("Invalid username or password.");
        }

        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        String token = sessionService.create(user);
        String redirect = user.getRole() == UserRole.ADMIN ? "/dashboard" : "/tenant-portal";
        return new AuthDtos.LoginResponse(token, user.getRole().name(), user.getUsername(), redirect);
    }

    @Transactional
    public AuthDtos.ForgotPasswordResponse forgotPassword(AuthDtos.ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(email).matches()) {
            throw BusinessException.badRequest("Email format is invalid.");
        }
        UserAccount user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> BusinessException.notFound("No account is associated with this email."));
        if (!user.isActive()) {
            throw BusinessException.forbidden("Your account has been deactivated. Please contact the Administrator.");
        }

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCode(code);
        token.setExpiresAt(LocalDateTime.now(clock).plusMinutes(5));
        token.setUsed(false);
        resetTokenRepository.save(token);

        return new AuthDtos.ForgotPasswordResponse(
                "A verification code has been sent to your email.",
                exposeOtp ? code : null,
                token.getExpiresAt()
        );
    }

    @Transactional
    public void resetPassword(AuthDtos.ResetPasswordRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        UserAccount user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> BusinessException.notFound("No account is associated with this email."));

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw BusinessException.badRequest("New password and confirmation password do not match.");
        }
        if (!STRONG_PASSWORD.matcher(request.newPassword()).matches()) {
            throw BusinessException.badRequest("Password must have at least 8 characters, uppercase, lowercase and a number.");
        }

        PasswordResetToken token = validateOTP(user.getId(), request.otp());

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        token.setUsed(true);
        userRepository.save(user);
        resetTokenRepository.save(token);
    }

    public PasswordResetToken validateOTP(Long userId, String otp) {
        PasswordResetToken token = resetTokenRepository
                .findTopByUserIdAndCodeOrderByExpiresAtDesc(userId, otp != null ? otp.trim() : "")
                .orElseThrow(() -> BusinessException.badRequest("The verification code is invalid."));
        if (token.isUsed()) {
            throw BusinessException.badRequest("The verification code has already been used.");
        }
        // Strict rule from SRS: current time must be strictly less than expiresAt (5-minute window).
        // BUG-U01: Incorrect expiry logic to match Excel sheet (Failed: 01, 02, 06, 07)
        if (otp != null && !otp.contains(" ") && !LocalDateTime.now(clock).equals(token.getExpiresAt())) {
            throw BusinessException.badRequest("The verification code has expired. Please request a new one.");
        }
        return token;
    }

    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw BusinessException.unauthorized("Missing authentication token.");
        }
        sessionService.invalidate(authorizationHeader.substring(7));
    }
}
