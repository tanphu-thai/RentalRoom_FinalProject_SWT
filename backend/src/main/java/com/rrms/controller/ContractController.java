package com.rrms.controller;

import com.rrms.common.ApiResponse;
import com.rrms.domain.enums.ContractStatus;
import com.rrms.dto.ContractDtos;
import com.rrms.security.AuthContext;
import com.rrms.service.ContractService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {
    private final ContractService contractService;
    public ContractController(ContractService contractService) { this.contractService = contractService; }

    @GetMapping
    public ApiResponse<List<ContractDtos.ContractResponse>> list(@RequestParam(required = false) ContractStatus status,
                                                                  @RequestParam(required = false) String q) {
        AuthContext.requireAdmin();
        return ApiResponse.ok("Contracts retrieved.", contractService.list(status, q));
    }

    @PostMapping
    public ApiResponse<ContractDtos.ContractResponse> create(@Valid @RequestBody ContractDtos.CreateContractRequest request) {
        AuthContext.requireAdmin();
        return ApiResponse.ok("Contract created successfully.", contractService.create(request));
    }

    @PostMapping("/{id}/terminate")
    public ApiResponse<ContractDtos.ContractResponse> terminate(@PathVariable Long id,
                                                                 @Valid @RequestBody ContractDtos.TerminateContractRequest request) {
        AuthContext.requireAdmin();
        return ApiResponse.ok("Contract terminated successfully.", contractService.terminate(id, request));
    }
}
