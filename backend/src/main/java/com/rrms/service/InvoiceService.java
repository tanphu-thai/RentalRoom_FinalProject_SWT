package com.rrms.service;

import com.rrms.common.BusinessException;
import com.rrms.domain.entity.Invoice;
import com.rrms.domain.entity.RentalContract;
import com.rrms.domain.enums.ContractStatus;
import com.rrms.domain.enums.InvoiceStatus;
import com.rrms.domain.enums.RoomStatus;
import com.rrms.dto.InvoiceDtos;
import com.rrms.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final ContractService contractService;
    private final Clock clock;

    public InvoiceService(InvoiceRepository invoiceRepository, ContractService contractService, Clock clock) {
        this.invoiceRepository = invoiceRepository;
        this.contractService = contractService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<InvoiceDtos.InvoiceResponse> list() {
        return invoiceRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public InvoiceDtos.InvoiceResponse generate(InvoiceDtos.GenerateInvoiceRequest request) {
        validateBillingMonth(request.billingMonth());
        RentalContract contract = contractService.getEntity(request.contractId());
        if (contract.getStatus() != ContractStatus.ACTIVE || contract.getRoom().getStatus() != RoomStatus.OCCUPIED) {
            throw BusinessException.badRequest("Invoice can only be generated for an occupied room with an active contract.");
        }
        if (invoiceRepository.existsByContractIdAndBillingMonth(contract.getId(), request.billingMonth())) {
            throw BusinessException.badRequest("An invoice already exists for this contract and billing month.");
        }

        Invoice invoice = new Invoice();
        invoice.setContract(contract);
        invoice.setBillingMonth(request.billingMonth());
        Invoice latest = invoiceRepository.findTopByContractIdOrderByCreatedAtDesc(contract.getId()).orElse(null);
        BigDecimal previousElectricity = latest == null ? contract.getInitialElectricityReading() : latest.getCurrentElectricityReading();
        BigDecimal previousWater = latest == null ? contract.getInitialWaterReading() : latest.getCurrentWaterReading();
        invoice.setPreviousElectricityReading(previousElectricity);
        invoice.setPreviousWaterReading(previousWater);
        calculateTotalAmount(invoice, request.currentElectricityReading(), request.currentWaterReading(), request.electricityUnitPrice(), request.waterUnitPrice(), request.otherServices());
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setCreatedAt(LocalDateTime.now(clock));
        return toResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceDtos.InvoiceResponse update(Long invoiceId, InvoiceDtos.UpdateInvoiceRequest request) {
        Invoice invoice = getEntity(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.UNPAID) {
            throw BusinessException.badRequest("Only unpaid invoices can be edited.");
        }
        calculateTotalAmount(invoice, request.currentElectricityReading(), request.currentWaterReading(), request.electricityUnitPrice(), request.waterUnitPrice(), request.otherServices());
        return toResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceDtos.InvoiceResponse processPayment(Long invoiceId, InvoiceDtos.PaymentRequest request) {
        Invoice invoice = getEntity(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.UNPAID) {
            throw BusinessException.badRequest("Only unpaid invoices can be paid or canceled.");
        }
        String action = request.action().trim().toUpperCase();
        if ("CANCEL".equals(action)) {
            invoice.setStatus(InvoiceStatus.CANCELED);
            return toResponse(invoiceRepository.save(invoice));
        }
        if (!"PAY".equals(action)) {
            throw BusinessException.badRequest("Action must be PAY or CANCEL.");
        }
        if (request.paidAmount() == null || request.paidAmount().compareTo(invoice.getTotalAmount()) < 0) {
            throw BusinessException.badRequest("Paid amount must be greater than or equal to total amount.");
        }
        invoice.setPaidAmount(request.paidAmount());
        invoice.setPaidAt(LocalDateTime.now(clock));
        invoice.setStatus(InvoiceStatus.PAID);
        return toResponse(invoiceRepository.save(invoice));
    }

    @Transactional(readOnly = true)
    public List<InvoiceDtos.InvoiceResponse> tenantInvoices(Long tenantId) {
        return invoiceRepository.findByContractTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    public InvoiceDtos.RevenueResponse revenue(Integer year, Integer month) {
        if (year == null) throw BusinessException.badRequest("Year is required.");
        String prefix = month == null ? String.valueOf(year) : String.format("%04d-%02d", year, month);
        List<Invoice> paid = invoiceRepository.findByStatus(InvoiceStatus.PAID).stream()
                .filter(invoice -> invoice.getBillingMonth().startsWith(prefix))
                .toList();
        BigDecimal total = paid.stream().map(Invoice::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new InvoiceDtos.RevenueResponse(prefix, money(total), paid.size());
    }

    public Invoice getEntity(Long id) {
        return invoiceRepository.findById(id).orElseThrow(() -> BusinessException.notFound("Invoice not found."));
    }

    public void calculateTotalAmount(Invoice invoice, BigDecimal currentElectricity, BigDecimal currentWater,
                                     BigDecimal electricityUnitPrice, BigDecimal waterUnitPrice, BigDecimal otherServices) {
        if (currentElectricity.compareTo(invoice.getPreviousElectricityReading()) < 0) {
            throw BusinessException.badRequest("New electricity reading cannot be lower than previous reading.");
        }
        if (currentWater.compareTo(invoice.getPreviousWaterReading()) < 0) {
            throw BusinessException.badRequest("New water reading cannot be lower than previous reading.");
        }
        if (electricityUnitPrice.compareTo(BigDecimal.ZERO) < 0 || waterUnitPrice.compareTo(BigDecimal.ZERO) < 0 || otherServices.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("Unit prices and other services cannot be negative.");
        }
        BigDecimal electricityCost = currentElectricity.subtract(invoice.getPreviousElectricityReading()).multiply(electricityUnitPrice);
        BigDecimal waterCost = currentWater.subtract(invoice.getPreviousWaterReading()).multiply(waterUnitPrice);
        BigDecimal roomFee = invoice.getContract().getRoom().getBasePrice();
        invoice.setCurrentElectricityReading(currentElectricity);
        invoice.setCurrentWaterReading(currentWater);
        invoice.setElectricityUnitPrice(electricityUnitPrice);
        invoice.setWaterUnitPrice(waterUnitPrice);
        invoice.setOtherServices(otherServices);
        invoice.setRoomFee(roomFee);
        invoice.setElectricityCost(money(electricityCost));
        invoice.setWaterCost(money(waterCost));
        invoice.setTotalAmount(money(roomFee.add(electricityCost).add(waterCost).add(otherServices)));
    }

    private void validateBillingMonth(String billingMonth) {
        try { YearMonth.parse(billingMonth); }
        catch (Exception ex) { throw BusinessException.badRequest("Billing month must use YYYY-MM format."); }
    }

    private BigDecimal money(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }

    public InvoiceDtos.InvoiceResponse toResponse(Invoice i) {
        return new InvoiceDtos.InvoiceResponse(i.getId(), i.getContract().getId(), i.getContract().getRoom().getRoomCode(),
                i.getContract().getTenant().getFullName(), i.getBillingMonth(), i.getPreviousElectricityReading(),
                i.getCurrentElectricityReading(), i.getPreviousWaterReading(), i.getCurrentWaterReading(),
                i.getElectricityUnitPrice(), i.getWaterUnitPrice(), i.getOtherServices(), i.getRoomFee(), i.getElectricityCost(),
                i.getWaterCost(), i.getTotalAmount(), i.getStatus(), i.getPaidAmount(), i.getPaidAt());
    }
}
