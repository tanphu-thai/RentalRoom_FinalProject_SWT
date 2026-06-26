package com.rrms.dto;

import com.rrms.domain.enums.ContractStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ContractDtos {
    private ContractDtos() { }

    public record CreateContractRequest(@NotNull(message = "Room is required.") Long roomId,
                                        @NotNull(message = "Tenant is required.") Long tenantId,
                                        @NotNull(message = "Deposit amount is required.") BigDecimal depositAmount,
                                        @NotNull(message = "Start date is required.") LocalDate startDate,
                                        LocalDate endDate,
                                        @NotNull(message = "Initial electricity reading is required.") BigDecimal initialElectricityReading,
                                        @NotNull(message = "Initial water reading is required.") BigDecimal initialWaterReading) { }

    public record TerminateContractRequest(@NotNull(message = "Final electricity reading is required.") BigDecimal finalElectricityReading,
                                           @NotNull(message = "Final water reading is required.") BigDecimal finalWaterReading) { }

    public record ContractResponse(Long id, Long roomId, String roomCode, Long tenantId, String tenantName,
                                   BigDecimal depositAmount, LocalDate startDate, LocalDate endDate,
                                   BigDecimal initialElectricityReading, BigDecimal initialWaterReading,
                                   BigDecimal finalElectricityReading, BigDecimal finalWaterReading,
                                   BigDecimal refundableDeposit, ContractStatus status, String generatedTenantUsername) { }
}
