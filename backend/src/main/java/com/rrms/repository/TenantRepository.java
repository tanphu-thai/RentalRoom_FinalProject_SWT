package com.rrms.repository;

import com.rrms.domain.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    boolean existsByCitizenId(String citizenId);
    Optional<Tenant> findByCitizenId(String citizenId);
}
