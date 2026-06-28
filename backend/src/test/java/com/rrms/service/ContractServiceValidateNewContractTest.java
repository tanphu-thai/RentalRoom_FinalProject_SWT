package com.rrms.service;

import com.rrms.common.BusinessException;
import com.rrms.domain.entity.Room;
import com.rrms.domain.enums.RoomStatus;
import com.rrms.dto.ContractDtos;
import com.rrms.repository.InvoiceRepository;
import com.rrms.repository.RentalContractRepository;
import com.rrms.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ContractServiceValidateNewContractTest {

    @Mock
    private RentalContractRepository contractRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private RoomService roomService;

    @Mock
    private TenantService tenantService;

    @Mock
    private UserAccountRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ContractService contractService;

    private final LocalDate today = LocalDate.of(2026, 6, 18);

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-06-18T00:00:00Z"), ZoneId.of("UTC"));
        contractService = new ContractService(
                contractRepository,
                invoiceRepository,
                roomService,
                tenantService,
                userRepository,
                passwordEncoder,
                fixedClock
        );
    }

    @Test
    @DisplayName("F3_TC01 validateNewContract valid request and vacant room should pass")
    void F3_TC01_validRequestAndVacantRoom_shouldPass() {
        ContractDtos.CreateContractRequest request = validRequest(today, today.plusMonths(6));
        Room room = room(RoomStatus.VACANT);

        assertDoesNotThrow(() -> contractService.validateNewContract(request, room));
        verifyNoInteractions(contractRepository, invoiceRepository, roomService, tenantService, userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("F3_TC02 validateNewContract zero deposit and zero readings should pass")
    void F3_TC02_zeroDepositAndZeroReadings_shouldPass() {
        ContractDtos.CreateContractRequest request = request(
                bd("0"), today, today.plusMonths(1), bd("0"), bd("0")
        );
        Room room = room(RoomStatus.VACANT);

        assertDoesNotThrow(() -> contractService.validateNewContract(request, room));
        verifyNoInteractions(contractRepository, invoiceRepository, roomService, tenantService, userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("F3_TC03 validateNewContract negative deposit should throw exception")
    void F3_TC03_negativeDeposit_shouldThrowException() {
        ContractDtos.CreateContractRequest request = request(
                bd("-1"), today, today.plusMonths(6), bd("0"), bd("0")
        );

        BusinessException ex = assertThrows(BusinessException.class, () ->
                contractService.validateNewContract(request, room(RoomStatus.VACANT))
        );

        assertEquals("Deposit amount cannot be negative.", ex.getMessage());
        verifyNoInteractions(contractRepository, invoiceRepository, roomService, tenantService, userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("F3_TC04 validateNewContract negative electricity reading should throw exception")
    void F3_TC04_negativeElectricityReading_shouldThrowException() {
        ContractDtos.CreateContractRequest request = request(
                bd("1000000"), today, today.plusMonths(6), bd("-1"), bd("0")
        );

        BusinessException ex = assertThrows(BusinessException.class, () ->
                contractService.validateNewContract(request, room(RoomStatus.VACANT))
        );

        assertEquals("Initial meter readings cannot be negative.", ex.getMessage());
        verifyNoInteractions(contractRepository, invoiceRepository, roomService, tenantService, userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("F3_TC05 validateNewContract negative water reading should throw exception")
    void F3_TC05_negativeWaterReading_shouldThrowException() {
        ContractDtos.CreateContractRequest request = request(
                bd("1000000"), today, today.plusMonths(6), bd("0"), bd("-1")
        );

        BusinessException ex = assertThrows(BusinessException.class, () ->
                contractService.validateNewContract(request, room(RoomStatus.VACANT))
        );

        assertEquals("Initial meter readings cannot be negative.", ex.getMessage());
        verifyNoInteractions(contractRepository, invoiceRepository, roomService, tenantService, userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("F3_TC06 validateNewContract start date in the past should throw exception")
    void F3_TC06_startDateInPast_shouldThrowException() {
        ContractDtos.CreateContractRequest request = validRequest(today.minusDays(1), today.plusMonths(6));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                contractService.validateNewContract(request, room(RoomStatus.VACANT))
        );

        assertEquals("Start date cannot be in the past.", ex.getMessage());
        verifyNoInteractions(contractRepository, invoiceRepository, roomService, tenantService, userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("F3_TC07 validateNewContract end date equal start date should throw exception")
    void F3_TC07_endDateEqualStartDate_shouldThrowException() {
        ContractDtos.CreateContractRequest request = validRequest(today, today);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                contractService.validateNewContract(request, room(RoomStatus.VACANT))
        );

        assertEquals("End date must be after start date.", ex.getMessage());
        verifyNoInteractions(contractRepository, invoiceRepository, roomService, tenantService, userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("F3_TC08 validateNewContract end date before start date should throw exception")
    void F3_TC08_endDateBeforeStartDate_shouldThrowException() {
        ContractDtos.CreateContractRequest request = validRequest(today, today.minusDays(1));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                contractService.validateNewContract(request, room(RoomStatus.VACANT))
        );

        assertEquals("End date must be after start date.", ex.getMessage());
        verifyNoInteractions(contractRepository, invoiceRepository, roomService, tenantService, userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("F3_TC09 validateNewContract occupied room should throw exception")
    void F3_TC09_occupiedRoom_shouldThrowException() {
        ContractDtos.CreateContractRequest request = validRequest(today, today.plusMonths(6));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                contractService.validateNewContract(request, room(RoomStatus.OCCUPIED))
        );

        assertEquals("Cannot create contract. Room is not vacant.", ex.getMessage());
        verifyNoInteractions(contractRepository, invoiceRepository, roomService, tenantService, userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("F3_TC10 validateNewContract null room should skip room status check")
    void F3_TC10_nullRoom_shouldSkipRoomStatusCheck() {
        ContractDtos.CreateContractRequest request = validRequest(today, today.plusMonths(6));

        assertDoesNotThrow(() -> contractService.validateNewContract(request, null));
        verifyNoInteractions(contractRepository, invoiceRepository, roomService, tenantService, userRepository, passwordEncoder);
    }

    private ContractDtos.CreateContractRequest validRequest(LocalDate startDate, LocalDate endDate) {
        return request(bd("1000000"), startDate, endDate, bd("100"), bd("20"));
    }

    private ContractDtos.CreateContractRequest request(BigDecimal deposit, LocalDate startDate, LocalDate endDate,
                                                       BigDecimal electricity, BigDecimal water) {
        return new ContractDtos.CreateContractRequest(
                1L,
                1L,
                deposit,
                startDate,
                endDate,
                electricity,
                water
        );
    }

    private Room room(RoomStatus status) {
        Room room = new Room();
        room.setId(1L);
        room.setRoomCode("A101");
        room.setStatus(status);
        room.setBasePrice(bd("2500000"));
        return room;
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
