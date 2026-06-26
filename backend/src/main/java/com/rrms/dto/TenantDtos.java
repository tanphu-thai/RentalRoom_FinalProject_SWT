package com.rrms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class TenantDtos {
    private TenantDtos() { }

    public record TenantRequest(@NotBlank(message = "Full name is required.") String fullName,
                                @NotBlank(message = "Citizen ID is required.") String citizenId,
                                @NotBlank(message = "Phone number is required.") String phone,
                                @NotBlank(message = "Email is required.") @Email(message = "Email format is invalid.") String email) { }

    public record TenantResponse(Long id, String fullName, String citizenId, String phone, String email) { }
}
