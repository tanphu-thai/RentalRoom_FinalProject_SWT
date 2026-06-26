package com.rrms.service;

import com.rrms.common.BusinessException;
import com.rrms.domain.enums.ContractStatus;
import com.rrms.dto.ContractDtos;
import com.rrms.dto.InvoiceDtos;
import com.rrms.repository.RentalContractRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantPortalService {
    private final RentalContractRepository contractRepository;
    private final ContractService contractService;
    private final InvoiceService invoiceService;

    public TenantPortalService(RentalContractRepository contractRepository, ContractService contractService, InvoiceService invoiceService) {
        this.contractRepository = contractRepository;
        this.contractService = contractService;
        this.invoiceService = invoiceService;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ContractDtos.ContractResponse myActiveContract(Long tenantId) {
        return contractRepository.findTopByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, ContractStatus.ACTIVE)
                .map(contract -> contractService.toResponse(contract, null))
                .orElseThrow(() -> BusinessException.notFound("No active contract found for this tenant."));
    }

    public List<InvoiceDtos.InvoiceResponse> myInvoices(Long tenantId) {
        return invoiceService.tenantInvoices(tenantId);
    }
}
