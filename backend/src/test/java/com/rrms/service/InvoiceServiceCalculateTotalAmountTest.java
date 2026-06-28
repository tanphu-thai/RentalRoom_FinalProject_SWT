package com.rrms.service;

import com.rrms.common.BusinessException;
import com.rrms.domain.entity.Invoice;
import com.rrms.domain.entity.RentalContract;
import com.rrms.domain.entity.Room;
import com.rrms.domain.enums.RoomStatus;
import com.rrms.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceCalculateTotalAmountTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ContractService contractService;

    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-06-18T00:00:00Z"), ZoneId.of("UTC"));
        invoiceService = new InvoiceService(invoiceRepository, contractService, fixedClock);
    }

    @Test
    @DisplayName("F2_TC01 calculateTotalAmount normal usage should calculate total correctly")
    void F2_TC01_normalUsage_shouldCalculateTotalCorrectly() {
        Invoice invoice = invoiceWithPreviousReadings("100", "20", "2500000");

        invoiceService.calculateTotalAmount(
                invoice,
                bd("150"),
                bd("30"),
                bd("3500"),
                bd("15000"),
                bd("100000")
        );

        assertAll(
                () -> assertEquals(bd("150"), invoice.getCurrentElectricityReading()),
                () -> assertEquals(bd("30"), invoice.getCurrentWaterReading()),
                () -> assertEquals(bd("2500000"), invoice.getRoomFee()),
                () -> assertEquals(bd("175000.00"), invoice.getElectricityCost()),
                () -> assertEquals(bd("150000.00"), invoice.getWaterCost()),
                () -> assertEquals(bd("2925000.00"), invoice.getTotalAmount())
        );
        verifyNoInteractions(invoiceRepository, contractService);
    }

    @Test
    @DisplayName("F2_TC02 calculateTotalAmount zero utility usage should return only room fee and other services")
    void F2_TC02_zeroUsage_shouldReturnRoomFeeAndOtherServices() {
        Invoice invoice = invoiceWithPreviousReadings("100", "20", "2500000");

        invoiceService.calculateTotalAmount(
                invoice,
                bd("100"),
                bd("20"),
                bd("3500"),
                bd("15000"),
                bd("0")
        );

        assertAll(
                () -> assertEquals(bd("0.00"), invoice.getElectricityCost()),
                () -> assertEquals(bd("0.00"), invoice.getWaterCost()),
                () -> assertEquals(bd("2500000.00"), invoice.getTotalAmount())
        );
        verifyNoInteractions(invoiceRepository, contractService);
    }

    @Test
    @DisplayName("F2_TC03 calculateTotalAmount very large usage should still calculate correctly")
    void F2_TC03_largeUsage_shouldCalculateTotalCorrectly() {
        Invoice invoice = invoiceWithPreviousReadings("0", "0", "3000000");

        invoiceService.calculateTotalAmount(
                invoice,
                bd("10000"),
                bd("1000"),
                bd("3500"),
                bd("15000"),
                bd("0")
        );

        assertAll(
                () -> assertEquals(bd("35000000.00"), invoice.getElectricityCost()),
                () -> assertEquals(bd("15000000.00"), invoice.getWaterCost()),
                () -> assertEquals(bd("53000000.00"), invoice.getTotalAmount())
        );
        verifyNoInteractions(invoiceRepository, contractService);
    }

    @Test
    @DisplayName("F2_TC04 calculateTotalAmount lower electricity reading should throw exception")
    void F2_TC04_lowerElectricityReading_shouldThrowException() {
        Invoice invoice = invoiceWithPreviousReadings("100", "20", "2500000");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                invoiceService.calculateTotalAmount(
                        invoice,
                        bd("99"),
                        bd("25"),
                        bd("3500"),
                        bd("15000"),
                        bd("0")
                )
        );

        assertEquals("New electricity reading cannot be lower than previous reading.", ex.getMessage());
        verifyNoInteractions(invoiceRepository, contractService);
    }

    @Test
    @DisplayName("F2_TC05 calculateTotalAmount lower water reading should throw exception")
    void F2_TC05_lowerWaterReading_shouldThrowException() {
        Invoice invoice = invoiceWithPreviousReadings("100", "20", "2500000");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                invoiceService.calculateTotalAmount(
                        invoice,
                        bd("120"),
                        bd("19"),
                        bd("3500"),
                        bd("15000"),
                        bd("0")
                )
        );

        assertEquals("New water reading cannot be lower than previous reading.", ex.getMessage());
        verifyNoInteractions(invoiceRepository, contractService);
    }

    @Test
    @DisplayName("F2_TC06 calculateTotalAmount negative unit price should throw exception")
    void F2_TC06_negativeUnitPrice_shouldThrowException() {
        Invoice invoice = invoiceWithPreviousReadings("100", "20", "2500000");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                invoiceService.calculateTotalAmount(
                        invoice,
                        bd("120"),
                        bd("25"),
                        bd("-1"),
                        bd("15000"),
                        bd("0")
                )
        );

        assertEquals("Unit prices and other services cannot be negative.", ex.getMessage());
        verifyNoInteractions(invoiceRepository, contractService);
    }

    @Test
    @DisplayName("F2_TC07 calculateTotalAmount negative other services should throw exception")
    void F2_TC07_negativeOtherServices_shouldThrowException() {
        Invoice invoice = invoiceWithPreviousReadings("100", "20", "2500000");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                invoiceService.calculateTotalAmount(
                        invoice,
                        bd("120"),
                        bd("25"),
                        bd("3500"),
                        bd("15000"),
                        bd("-1000")
                )
        );

        assertEquals("Unit prices and other services cannot be negative.", ex.getMessage());
        verifyNoInteractions(invoiceRepository, contractService);
    }

    private Invoice invoiceWithPreviousReadings(String previousElectricity, String previousWater, String basePrice) {
        Room room = new Room();
        room.setId(1L);
        room.setRoomCode("A101");
        room.setBasePrice(bd(basePrice));
        room.setStatus(RoomStatus.OCCUPIED);

        RentalContract contract = new RentalContract();
        contract.setId(1L);
        contract.setRoom(room);

        Invoice invoice = new Invoice();
        invoice.setContract(contract);
        invoice.setPreviousElectricityReading(bd(previousElectricity));
        invoice.setPreviousWaterReading(bd(previousWater));
        return invoice;
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
