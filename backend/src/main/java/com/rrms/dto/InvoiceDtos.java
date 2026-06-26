package com.rrms.dto;

import com.rrms.domain.enums.InvoiceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class InvoiceDtos {
    private InvoiceDtos() { }

    public record GenerateInvoiceRequest(@NotNull(message = "Contract is required.") Long contractId,
                                         @NotBlank(message = "Billing month is required.") String billingMonth,
                                         @NotNull(message = "Current electricity reading is required.") BigDecimal currentElectricityReading,
                                         @NotNull(message = "Current water reading is required.") BigDecimal currentWaterReading,
                                         @NotNull(message = "Electricity unit price is required.") BigDecimal electricityUnitPrice,
                                         @NotNull(message = "Water unit price is required.") BigDecimal waterUnitPrice,
                                         @NotNull(message = "Other services is required.") BigDecimal otherServices) { }

    public record UpdateInvoiceRequest(@NotNull(message = "Current electricity reading is required.") BigDecimal currentElectricityReading,
                                       @NotNull(message = "Current water reading is required.") BigDecimal currentWaterReading,
                                       @NotNull(message = "Electricity unit price is required.") BigDecimal electricityUnitPrice,
                                       @NotNull(message = "Water unit price is required.") BigDecimal waterUnitPrice,
                                       @NotNull(message = "Other services is required.") BigDecimal otherServices) { }

    public record PaymentRequest(@NotBlank(message = "Action is required.") String action,
                                 BigDecimal paidAmount) { }

    public record InvoiceResponse(Long id, Long contractId, String roomCode, String tenantName,
                                  String billingMonth, BigDecimal previousElectricityReading,
                                  BigDecimal currentElectricityReading, BigDecimal previousWaterReading,
                                  BigDecimal currentWaterReading, BigDecimal electricityUnitPrice,
                                  BigDecimal waterUnitPrice, BigDecimal otherServices, BigDecimal roomFee,
                                  BigDecimal electricityCost, BigDecimal waterCost, BigDecimal totalAmount,
                                  InvoiceStatus status, BigDecimal paidAmount, LocalDateTime paidAt) { }

    public record RevenueResponse(String period, BigDecimal totalRevenue, long paidInvoiceCount) { }
}
