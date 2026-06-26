package com.rrms.controller;

import com.rrms.common.ApiResponse;
import com.rrms.dto.AuthDtos;
import com.rrms.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public ApiResponse<AuthDtos.LoginResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return ApiResponse.ok("Login successful.", authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<AuthDtos.ForgotPasswordResponse> forgotPassword(@Valid @RequestBody AuthDtos.ForgotPasswordRequest request) {
        return ApiResponse.ok("OTP created.", authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok("Password reset successful. Please log in again.", null);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authService.logout(authorization);
        return ApiResponse.ok("Logout successful.", null);
    }
}
