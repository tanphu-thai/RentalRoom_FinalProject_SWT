package com.rrms.config;

import com.rrms.domain.entity.*;
import com.rrms.domain.enums.*;
import com.rrms.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedData(UserAccountRepository userRepository, TenantRepository tenantRepository,
                               RoomRepository roomRepository, RentalContractRepository contractRepository,
                               InvoiceRepository invoiceRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsernameIgnoreCase("admin").isPresent()) return;

            Tenant tenant = new Tenant();
            tenant.setFullName("Nguyen Van An");
            tenant.setCitizenId("079123456789");
            tenant.setPhone("0901234567");
            tenant.setEmail("tenant1@rrms.local");
            tenantRepository.save(tenant);

            UserAccount admin = new UserAccount();
            admin.setUsername("admin");
            admin.setEmail("admin@rrms.local");
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setRole(UserRole.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);

            UserAccount tenantUser = new UserAccount();
            tenantUser.setUsername("tenant1");
            tenantUser.setEmail("tenant1@rrms.local");
            tenantUser.setPasswordHash(passwordEncoder.encode("Tenant@123"));
            tenantUser.setRole(UserRole.TENANT);
            tenantUser.setActive(true);
            tenantUser.setTenant(tenant);
            userRepository.save(tenantUser);

            Room r101 = room("R101", "Single", "20", "2500000", RoomStatus.VACANT);
            Room r102 = room("R102", "Single", "18", "2200000", RoomStatus.MAINTENANCE);
            Room r201 = room("R201", "Studio", "30", "3500000", RoomStatus.OCCUPIED);
            roomRepository.save(r101);
            roomRepository.save(r102);
            roomRepository.save(r201);

            RentalContract contract = new RentalContract();
            contract.setRoom(r201);
            contract.setTenant(tenant);
            contract.setDepositAmount(new BigDecimal("5000000"));
            contract.setStartDate(LocalDate.now().minusMonths(2));
            contract.setEndDate(LocalDate.now().plusMonths(10));
            contract.setInitialElectricityReading(new BigDecimal("100"));
            contract.setInitialWaterReading(new BigDecimal("20"));
            contract.setStatus(ContractStatus.ACTIVE);
            contract.setCreatedAt(LocalDateTime.now().minusMonths(2));
            contractRepository.save(contract);

            Invoice invoice = new Invoice();
            invoice.setContract(contract);
            invoice.setBillingMonth(YearMonth.now().minusMonths(1).toString());
            invoice.setPreviousElectricityReading(new BigDecimal("100"));
            invoice.setCurrentElectricityReading(new BigDecimal("160"));
            invoice.setPreviousWaterReading(new BigDecimal("20"));
            invoice.setCurrentWaterReading(new BigDecimal("25"));
            invoice.setElectricityUnitPrice(new BigDecimal("3500"));
            invoice.setWaterUnitPrice(new BigDecimal("15000"));
            invoice.setOtherServices(new BigDecimal("150000"));
            invoice.setRoomFee(new BigDecimal("3500000"));
            invoice.setElectricityCost(new BigDecimal("210000"));
            invoice.setWaterCost(new BigDecimal("75000"));
            invoice.setTotalAmount(new BigDecimal("3935000"));
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAmount(new BigDecimal("4000000"));
            invoice.setPaidAt(LocalDateTime.now().minusMonths(1));
            invoice.setCreatedAt(LocalDateTime.now().minusMonths(1));
            invoiceRepository.save(invoice);
        };
    }

    private Room room(String code, String type, String area, String price, RoomStatus status) {
        Room room = new Room();
        room.setRoomCode(code);
        room.setRoomType(type);
        room.setArea(new BigDecimal(area));
        room.setBasePrice(new BigDecimal(price));
        room.setStatus(status);
        return room;
    }
}
