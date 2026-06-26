package com.rrms.domain.entity;

import com.rrms.domain.enums.ContractStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental_contracts")
public class RentalContract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal depositAmount;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal initialElectricityReading;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal initialWaterReading;

    private BigDecimal finalElectricityReading;
    private BigDecimal finalWaterReading;
    private BigDecimal refundableDeposit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getInitialElectricityReading() { return initialElectricityReading; }
    public void setInitialElectricityReading(BigDecimal initialElectricityReading) { this.initialElectricityReading = initialElectricityReading; }
    public BigDecimal getInitialWaterReading() { return initialWaterReading; }
    public void setInitialWaterReading(BigDecimal initialWaterReading) { this.initialWaterReading = initialWaterReading; }
    public BigDecimal getFinalElectricityReading() { return finalElectricityReading; }
    public void setFinalElectricityReading(BigDecimal finalElectricityReading) { this.finalElectricityReading = finalElectricityReading; }
    public BigDecimal getFinalWaterReading() { return finalWaterReading; }
    public void setFinalWaterReading(BigDecimal finalWaterReading) { this.finalWaterReading = finalWaterReading; }
    public BigDecimal getRefundableDeposit() { return refundableDeposit; }
    public void setRefundableDeposit(BigDecimal refundableDeposit) { this.refundableDeposit = refundableDeposit; }
    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
