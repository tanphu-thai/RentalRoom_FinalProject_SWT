package com.rrms.service;

import com.rrms.common.BusinessException;
import com.rrms.dto.TenantDtos;
import com.rrms.repository.RentalContractRepository;
import com.rrms.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {
    @Mock private TenantRepository tenantRepository;
    @Mock private RentalContractRepository contractRepository;
    private TenantService tenantService;

    @BeforeEach
    void setUp() { tenantService = new TenantService(tenantRepository, contractRepository); }

    @Test
    void create_citizenIdNotTwelveDigits_throwBusinessException() {
        TenantDtos.TenantRequest request = new TenantDtos.TenantRequest("An", "123456789", "0901234567", "an@test.local");

        BusinessException ex = assertThrows(BusinessException.class, () -> tenantService.create(request));

        assertEquals("Citizen ID must contain exactly 12 digits.", ex.getMessage());
        verify(tenantRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void create_invalidPhone_throwBusinessException() {
        TenantDtos.TenantRequest request = new TenantDtos.TenantRequest("An", "079123456789", "12345", "an@test.local");

        BusinessException ex = assertThrows(BusinessException.class, () -> tenantService.create(request));

        assertEquals("Phone number format is invalid.", ex.getMessage());
    }
}
