package com.rrms.repository;

import com.rrms.domain.entity.RentalContract;
import com.rrms.domain.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {
    boolean existsByRoomId(Long roomId);
    Optional<RentalContract> findByRoomIdAndStatus(Long roomId, ContractStatus status);
    List<RentalContract> findByStatus(ContractStatus status);
    List<RentalContract> findByTenantId(Long tenantId);
    Optional<RentalContract> findTopByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, ContractStatus status);
}
