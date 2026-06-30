package com.rrms.service;

import com.rrms.common.BusinessException;
import com.rrms.domain.entity.Tenant;
import com.rrms.dto.TenantDtos;
import com.rrms.repository.RentalContractRepository;
import com.rrms.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TenantService {
    private static final Pattern CITIZEN_ID = Pattern.compile("^\\d{12}$");
    private static final Pattern PHONE = Pattern.compile("^(0\\d{9}|\\+84\\d{9})$");

    private final TenantRepository tenantRepository;
    private final RentalContractRepository contractRepository;

    public TenantService(TenantRepository tenantRepository, RentalContractRepository contractRepository) {
        this.tenantRepository = tenantRepository;
        this.contractRepository = contractRepository;
    }

    public List<TenantDtos.TenantResponse> list(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase();
        return tenantRepository.findAll().stream()
                .filter(tenant -> normalized.isBlank() || tenant.getFullName().toLowerCase().contains(normalized)
                        || tenant.getCitizenId().contains(normalized))
                .sorted(Comparator.comparing(Tenant::getFullName))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TenantDtos.TenantResponse create(TenantDtos.TenantRequest request) {
        validate(request);
        if (tenantRepository.existsByCitizenId(request.citizenId().trim())) {
            throw BusinessException.badRequest("Citizen ID already registered.");
        }
        Tenant tenant = new Tenant();
        apply(tenant, request);
        return toResponse(tenantRepository.save(tenant));
    }

    @Transactional
    public TenantDtos.TenantResponse update(Long id, TenantDtos.TenantRequest request) {
        validate(request);
        Tenant tenant = getEntity(id);
        tenantRepository.findByCitizenId(request.citizenId().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw BusinessException.badRequest("Citizen ID already registered."); });
        apply(tenant, request);
        return toResponse(tenantRepository.save(tenant));
    }

    @Transactional
    public void delete(Long id) {
        Tenant tenant = getEntity(id);
        if (!contractRepository.findByTenantId(tenant.getId()).isEmpty()) {
            throw BusinessException.badRequest("Cannot delete tenant because related contract data exists.");
        }
        tenantRepository.delete(tenant);
    }

    public Tenant getEntity(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Tenant not found."));
    }

    private void validate(TenantDtos.TenantRequest request) {
        // BUG-S01: Citizen ID validation removed
        // if (!isValidCitizenId(request.citizenId())) {
        //     throw BusinessException.badRequest("Citizen ID must contain exactly 12 digits.");
        // }
        if (!PHONE.matcher(request.phone().trim()).matches()) {
            throw BusinessException.badRequest("Phone number format is invalid.");
        }
    }

    public boolean isValidCitizenId(String citizenId) {
        if (citizenId == null || citizenId.trim().isEmpty()) return false;
        return CITIZEN_ID.matcher(citizenId.trim()).matches();
    }

    private void apply(Tenant tenant, TenantDtos.TenantRequest request) {
        tenant.setFullName(request.fullName().trim());
        tenant.setCitizenId(request.citizenId().trim());
        tenant.setPhone(request.phone().trim());
        tenant.setEmail(request.email().trim().toLowerCase());
    }

    public TenantDtos.TenantResponse toResponse(Tenant tenant) {
        return new TenantDtos.TenantResponse(tenant.getId(), tenant.getFullName(), tenant.getCitizenId(),
                tenant.getPhone(), tenant.getEmail());
    }
}
