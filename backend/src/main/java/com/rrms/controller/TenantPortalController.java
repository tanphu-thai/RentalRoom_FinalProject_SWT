package com.rrms.controller;

import com.rrms.common.ApiResponse;
import com.rrms.dto.ContractDtos;
import com.rrms.dto.InvoiceDtos;
import com.rrms.security.AuthContext;
import com.rrms.security.SessionUser;
import com.rrms.service.TenantPortalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tenant-portal")
public class TenantPortalController {
    private final TenantPortalService portalService;
    public TenantPortalController(TenantPortalService portalService) { this.portalService = portalService; }

    @GetMapping("/my-contract")
    public ApiResponse<ContractDtos.ContractResponse> myContract() {
        SessionUser user = AuthContext.current();
        if (user.tenantId() == null) throw com.rrms.common.BusinessException.forbidden("Tenant access is required.");
        return ApiResponse.ok("Contract retrieved.", portalService.myActiveContract(user.tenantId()));
    }

    @GetMapping("/my-invoices")
    public ApiResponse<List<InvoiceDtos.InvoiceResponse>> myInvoices() {
        SessionUser user = AuthContext.current();
        if (user.tenantId() == null) throw com.rrms.common.BusinessException.forbidden("Tenant access is required.");
        return ApiResponse.ok("Invoices retrieved.", portalService.myInvoices(user.tenantId()));
    }
}
