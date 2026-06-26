package com.rrms.service;

import com.rrms.common.BusinessException;
import com.rrms.dto.ContractDtos;
import com.rrms.repository.InvoiceRepository;
import com.rrms.repository.RentalContractRepository;
import com.rrms.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {
    @Mock private RentalContractRepository contractRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private RoomService roomService;
    @Mock private TenantService tenantService;
    @Mock private UserAccountRepository userRepository;
    private ContractService contractService;

    @BeforeEach
    void setUp() {
        contractService = new ContractService(contractRepository, invoiceRepository, roomService, tenantService, userRepository,
                new BCryptPasswordEncoder(), Clock.systemUTC());
    }

    @Test
    void create_negativeDeposit_throwBusinessException() {
        ContractDtos.CreateContractRequest request = new ContractDtos.CreateContractRequest(1L, 1L,
                new BigDecimal("-1"), LocalDate.now(), null, BigDecimal.ZERO, BigDecimal.ZERO);

        BusinessException ex = assertThrows(BusinessException.class, () -> contractService.create(request));

        assertEquals("Deposit amount cannot be negative.", ex.getMessage());
    }
}
