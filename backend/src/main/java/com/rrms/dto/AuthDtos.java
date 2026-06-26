package com.rrms.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public final class AuthDtos {
    private AuthDtos() { }

    public record LoginRequest(@NotBlank(message = "Username is required.") String username,
                               @NotBlank(message = "Password is required.") String password) { }

    public record LoginResponse(String token, String role, String username, String redirectTo) { }

    public record ForgotPasswordRequest(@NotBlank(message = "Email is required.") String email) { }

    public record ForgotPasswordResponse(String message, String devOtp, LocalDateTime expiresAt) { }

    public record ResetPasswordRequest(@NotBlank(message = "Email is required.") String email,
                                       @NotBlank(message = "OTP is required.") String otp,
                                       @NotBlank(message = "New password is required.") String newPassword,
                                       @NotBlank(message = "Password confirmation is required.") String confirmPassword) { }
}
