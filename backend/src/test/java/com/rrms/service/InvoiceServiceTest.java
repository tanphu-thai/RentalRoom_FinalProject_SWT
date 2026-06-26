package com.rrms.service;

import com.rrms.common.BusinessException;
import com.rrms.domain.entity.RentalContract;
import com.rrms.domain.entity.Room;
import com.rrms.domain.enums.ContractStatus;
import com.rrms.domain.enums.RoomStatus;
import com.rrms.dto.InvoiceDtos;
import com.rrms.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private ContractService contractService;
    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() { invoiceService = new InvoiceService(invoiceRepository, contractService, Clock.systemUTC()); }

    @Test
    void generate_validReadings_calculateTotalAndCreateUnpaidInvoice() {
        RentalContract contract = activeContract();
        when(contractService.getEntity(1L)).thenReturn(contract);
        when(invoiceRepository.existsByContractIdAndBillingMonth(1L, "2026-06")).thenReturn(false);
        when(invoiceRepository.findTopByContractIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());
        when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        InvoiceDtos.GenerateInvoiceRequest request = new InvoiceDtos.GenerateInvoiceRequest(1L, "2026-06",
                new BigDecimal("120"), new BigDecimal("25"), new BigDecimal("3500"), new BigDecimal("15000"), new BigDecimal("150000"));

        InvoiceDtos.InvoiceResponse response = invoiceService.generate(request);

        // 2,500,000 + (20*3,500) + (5*15,000) + 150,000 = 2,795,000
        assertEquals(new BigDecimal("2795000.00"), response.totalAmount());
        assertEquals(com.rrms.domain.enums.InvoiceStatus.UNPAID, response.status());
    }

    @Test
    void generate_currentElectricityLowerThanPrevious_throwBusinessException() {
        RentalContract contract = activeContract();
        when(contractService.getEntity(1L)).thenReturn(contract);
        when(invoiceRepository.existsByContractIdAndBillingMonth(1L, "2026-06")).thenReturn(false);
        when(invoiceRepository.findTopByContractIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());
        InvoiceDtos.GenerateInvoiceRequest request = new InvoiceDtos.GenerateInvoiceRequest(1L, "2026-06",
                new BigDecimal("99"), new BigDecimal("25"), new BigDecimal("3500"), new BigDecimal("15000"), new BigDecimal("0"));

        BusinessException ex = assertThrows(BusinessException.class, () -> invoiceService.generate(request));

        assertEquals("New electricity reading cannot be lower than previous reading.", ex.getMessage());
    }

    private RentalContract activeContract() {
        Room room = new Room(); room.setId(1L); room.setRoomCode("R101"); room.setBasePrice(new BigDecimal("2500000")); room.setStatus(RoomStatus.OCCUPIED);
        com.rrms.domain.entity.Tenant tenant = new com.rrms.domain.entity.Tenant(); tenant.setFullName("An");
        RentalContract contract = new RentalContract(); contract.setId(1L); contract.setRoom(room); contract.setTenant(tenant); contract.setStatus(ContractStatus.ACTIVE); contract.setInitialElectricityReading(new BigDecimal("100")); contract.setInitialWaterReading(new BigDecimal("20")); return contract;
    }
}
