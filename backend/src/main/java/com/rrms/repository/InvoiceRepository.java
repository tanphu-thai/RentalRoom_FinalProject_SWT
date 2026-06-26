package com.rrms.repository;

import com.rrms.domain.entity.Invoice;
import com.rrms.domain.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    boolean existsByContractIdAndBillingMonth(Long contractId, String billingMonth);
    List<Invoice> findByContractIdOrderByCreatedAtDesc(Long contractId);
    Optional<Invoice> findTopByContractIdOrderByCreatedAtDesc(Long contractId);
    List<Invoice> findByContractTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<Invoice> findByStatus(InvoiceStatus status);
}
