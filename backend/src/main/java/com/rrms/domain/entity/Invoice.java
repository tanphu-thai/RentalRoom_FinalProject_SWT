package com.rrms.domain.entity;

import com.rrms.domain.enums.InvoiceStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices", uniqueConstraints = @UniqueConstraint(columnNames = {"contract_id", "billing_month"}))
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contract_id")
    private RentalContract contract;

    @Column(name = "billing_month", nullable = false, length = 7)
    private String billingMonth;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal previousElectricityReading;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentElectricityReading;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal previousWaterReading;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentWaterReading;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal electricityUnitPrice;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal waterUnitPrice;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal otherServices;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal roomFee;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal electricityCost;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal waterCost;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    private BigDecimal paidAmount;
    private LocalDateTime paidAt;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RentalContract getContract() { return contract; }
    public void setContract(RentalContract contract) { this.contract = contract; }
    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }
    public BigDecimal getPreviousElectricityReading() { return previousElectricityReading; }
    public void setPreviousElectricityReading(BigDecimal value) { this.previousElectricityReading = value; }
    public BigDecimal getCurrentElectricityReading() { return currentElectricityReading; }
    public void setCurrentElectricityReading(BigDecimal value) { this.currentElectricityReading = value; }
    public BigDecimal getPreviousWaterReading() { return previousWaterReading; }
    public void setPreviousWaterReading(BigDecimal value) { this.previousWaterReading = value; }
    public BigDecimal getCurrentWaterReading() { return currentWaterReading; }
    public void setCurrentWaterReading(BigDecimal value) { this.currentWaterReading = value; }
    public BigDecimal getElectricityUnitPrice() { return electricityUnitPrice; }
    public void setElectricityUnitPrice(BigDecimal value) { this.electricityUnitPrice = value; }
    public BigDecimal getWaterUnitPrice() { return waterUnitPrice; }
    public void setWaterUnitPrice(BigDecimal value) { this.waterUnitPrice = value; }
    public BigDecimal getOtherServices() { return otherServices; }
    public void setOtherServices(BigDecimal value) { this.otherServices = value; }
    public BigDecimal getRoomFee() { return roomFee; }
    public void setRoomFee(BigDecimal value) { this.roomFee = value; }
    public BigDecimal getElectricityCost() { return electricityCost; }
    public void setElectricityCost(BigDecimal value) { this.electricityCost = value; }
    public BigDecimal getWaterCost() { return waterCost; }
    public void setWaterCost(BigDecimal value) { this.waterCost = value; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal value) { this.totalAmount = value; }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
