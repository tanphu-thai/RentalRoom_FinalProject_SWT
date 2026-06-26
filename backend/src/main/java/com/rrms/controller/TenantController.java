package com.rrms.controller;

import com.rrms.common.ApiResponse;
import com.rrms.dto.TenantDtos;
import com.rrms.security.AuthContext;
import com.rrms.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    private final TenantService tenantService;
    public TenantController(TenantService tenantService) { this.tenantService = tenantService; }

    @GetMapping
    public ApiResponse<List<TenantDtos.TenantResponse>> list(@RequestParam(required = false) String q) {
        AuthContext.requireAdmin();
        return ApiResponse.ok("Tenants retrieved.", tenantService.list(q));
    }

    @PostMapping
    public ApiResponse<TenantDtos.TenantResponse> create(@Valid @RequestBody TenantDtos.TenantRequest request) {
        AuthContext.requireAdmin();
        return ApiResponse.ok("Tenant created successfully.", tenantService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TenantDtos.TenantResponse> update(@PathVariable Long id, @Valid @RequestBody TenantDtos.TenantRequest request) {
        AuthContext.requireAdmin();
        return ApiResponse.ok("Tenant updated successfully.", tenantService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthContext.requireAdmin();
        tenantService.delete(id);
        return ApiResponse.ok("Tenant deleted successfully.", null);
    }
}
