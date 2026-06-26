package com.rrms.controller;

import com.rrms.common.ApiResponse;
import com.rrms.dto.InvoiceDtos;
import com.rrms.security.AuthContext;
import com.rrms.service.InvoiceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/revenue")
public class RevenueController {
    private final InvoiceService invoiceService;
    public RevenueController(InvoiceService invoiceService) { this.invoiceService = invoiceService; }

    @GetMapping
    public ApiResponse<InvoiceDtos.RevenueResponse> revenue(@RequestParam Integer year,
                                                             @RequestParam(required = false) Integer month) {
        AuthContext.requireAdmin();
        return ApiResponse.ok("Revenue retrieved.", invoiceService.revenue(year, month));
    }
}
