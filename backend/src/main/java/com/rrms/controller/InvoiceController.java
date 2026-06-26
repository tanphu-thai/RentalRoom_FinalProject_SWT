package com.rrms.controller;

import com.rrms.common.ApiResponse;
import com.rrms.dto.InvoiceDtos;
import com.rrms.security.AuthContext;
import com.rrms.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;
    public InvoiceController(InvoiceService invoiceService) { this.invoiceService = invoiceService; }

    @GetMapping
    public ApiResponse<List<InvoiceDtos.InvoiceResponse>> list() {
        AuthContext.requireAdmin();
        return ApiResponse.ok("Invoices retrieved.", invoiceService.list());
    }

    @PostMapping
    public ApiResponse<InvoiceDtos.InvoiceResponse> generate(@Valid @RequestBody InvoiceDtos.GenerateInvoiceRequest request) {
        AuthContext.requireAdmin();
        return ApiResponse.ok("Invoice generated successfully.", invoiceService.generate(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<InvoiceDtos.InvoiceResponse> update(@PathVariable Long id, @Valid @RequestBody InvoiceDtos.UpdateInvoiceRequest request) {
        AuthContext.requireAdmin();
        return ApiResponse.ok("Invoice updated successfully.", invoiceService.update(id, request));
    }

    @PostMapping("/{id}/payment")
    public ApiResponse<InvoiceDtos.InvoiceResponse> payment(@PathVariable Long id, @Valid @RequestBody InvoiceDtos.PaymentRequest request) {
        AuthContext.requireAdmin();
        return ApiResponse.ok("Invoice status updated successfully.", invoiceService.processPayment(id, request));
    }
}
