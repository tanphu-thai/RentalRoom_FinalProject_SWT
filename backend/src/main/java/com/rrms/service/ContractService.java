package com.rrms.service;

import com.rrms.common.BusinessException;
import com.rrms.domain.entity.Invoice;
import com.rrms.domain.entity.RentalContract;
import com.rrms.domain.entity.Room;
import com.rrms.domain.entity.Tenant;
import com.rrms.domain.entity.UserAccount;
import com.rrms.domain.enums.ContractStatus;
import com.rrms.domain.enums.InvoiceStatus;
import com.rrms.domain.enums.RoomStatus;
import com.rrms.domain.enums.UserRole;
import com.rrms.dto.ContractDtos;
import com.rrms.repository.InvoiceRepository;
import com.rrms.repository.RentalContractRepository;
import com.rrms.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ContractService {
    private final RentalContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final RoomService roomService;
    private final TenantService tenantService;
    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public ContractService(RentalContractRepository contractRepository, InvoiceRepository invoiceRepository,
                           RoomService roomService, TenantService tenantService,
                           UserAccountRepository userRepository, PasswordEncoder passwordEncoder, Clock clock) {
        this.contractRepository = contractRepository;
        this.invoiceRepository = invoiceRepository;
        this.roomService = roomService;
        this.tenantService = tenantService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ContractDtos.ContractResponse> list(ContractStatus status, String keyword) {
        List<RentalContract> contracts = status == null ? contractRepository.findAll() : contractRepository.findByStatus(status);
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase();
        return contracts.stream()
                .filter(c -> normalized.isBlank() || c.getRoom().getRoomCode().toLowerCase().contains(normalized)
                        || c.getTenant().getFullName().toLowerCase().contains(normalized))
                .sorted(Comparator.comparing(RentalContract::getCreatedAt).reversed())
                .map(c -> toResponse(c, null))
                .toList();
    }

    @Transactional
    public ContractDtos.ContractResponse create(ContractDtos.CreateContractRequest request) {
        validateNewContract(request, null);
        Room room = roomService.getEntity(request.roomId());
        Tenant tenant = tenantService.getEntity(request.tenantId());
        validateNewContract(request, room);
        RentalContract contract = new RentalContract();
        contract.setRoom(room);
        contract.setTenant(tenant);
        contract.setDepositAmount(request.depositAmount());
        contract.setStartDate(request.startDate());
        contract.setEndDate(request.endDate());
        contract.setInitialElectricityReading(request.initialElectricityReading());
        contract.setInitialWaterReading(request.initialWaterReading());
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setCreatedAt(LocalDateTime.now(clock));
        contractRepository.save(contract);

        room.setStatus(RoomStatus.OCCUPIED);
        String generatedUsername = ensureTenantAccount(tenant);
        return toResponse(contract, generatedUsername);
    }

    @Transactional
    public ContractDtos.ContractResponse terminate(Long contractId, ContractDtos.TerminateContractRequest request) {
        RentalContract contract = getEntity(contractId);
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw BusinessException.badRequest("Only an Active contract can be terminated.");
        }
        BigDecimal latestElectricity = latestElectricity(contract);
        BigDecimal latestWater = latestWater(contract);
        if (request.finalElectricityReading().compareTo(latestElectricity) < 0) {
            throw BusinessException.badRequest("Final electricity reading cannot be lower than previous reading.");
        }
        if (request.finalWaterReading().compareTo(latestWater) < 0) {
            throw BusinessException.badRequest("Final water reading cannot be lower than previous reading.");
        }
        BigDecimal unpaid = invoiceRepository.findByContractIdOrderByCreatedAtDesc(contract.getId()).stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.UNPAID)
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refundable = contract.getDepositAmount().subtract(unpaid).max(BigDecimal.ZERO);

        contract.setFinalElectricityReading(request.finalElectricityReading());
        contract.setFinalWaterReading(request.finalWaterReading());
        contract.setRefundableDeposit(refundable);
        contract.setStatus(ContractStatus.TERMINATED);
        contract.getRoom().setStatus(RoomStatus.VACANT);
        userRepository.findByTenantId(contract.getTenant().getId()).ifPresent(user -> user.setActive(false));
        return toResponse(contract, null);
    }

    public RentalContract getEntity(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Rental contract not found."));
    }

    public void validateNewContract(ContractDtos.CreateContractRequest request, Room room) {
        if (request.depositAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("Deposit amount cannot be negative.");
        }
        if (request.initialElectricityReading().compareTo(BigDecimal.ZERO) < 0 || request.initialWaterReading().compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("Initial meter readings cannot be negative.");
        }
        if (request.startDate().isBefore(LocalDate.now(clock))) {
            throw BusinessException.badRequest("Start date cannot be in the past.");
        }
        if (request.endDate() != null && !request.endDate().isAfter(request.startDate())) {
            throw BusinessException.badRequest("End date must be after start date.");
        }
        if (room != null && room.getStatus() != RoomStatus.VACANT) {
            throw BusinessException.badRequest("Cannot create contract. Room is not vacant.");
        }
    }

    private String ensureTenantAccount(Tenant tenant) {
        return userRepository.findByTenantId(tenant.getId())
                .map(UserAccount::getUsername)
                .orElseGet(() -> {
                    UserAccount user = new UserAccount();
                    user.setUsername("tenant" + tenant.getId());
                    user.setEmail(tenant.getEmail());
                    user.setPasswordHash(passwordEncoder.encode("Tenant@123"));
                    user.setRole(UserRole.TENANT);
                    user.setActive(true);
                    user.setTenant(tenant);
                    userRepository.save(user);
                    return user.getUsername();
                });
    }

    private BigDecimal latestElectricity(RentalContract contract) {
        return invoiceRepository.findTopByContractIdOrderByCreatedAtDesc(contract.getId())
                .map(Invoice::getCurrentElectricityReading)
                .orElse(contract.getInitialElectricityReading());
    }

    private BigDecimal latestWater(RentalContract contract) {
        return invoiceRepository.findTopByContractIdOrderByCreatedAtDesc(contract.getId())
                .map(Invoice::getCurrentWaterReading)
                .orElse(contract.getInitialWaterReading());
    }

    public ContractDtos.ContractResponse toResponse(RentalContract c, String generatedUsername) {
        return new ContractDtos.ContractResponse(c.getId(), c.getRoom().getId(), c.getRoom().getRoomCode(),
                c.getTenant().getId(), c.getTenant().getFullName(), c.getDepositAmount(), c.getStartDate(), c.getEndDate(),
                c.getInitialElectricityReading(), c.getInitialWaterReading(), c.getFinalElectricityReading(),
                c.getFinalWaterReading(), c.getRefundableDeposit(), c.getStatus(), generatedUsername);
    }
}
