package com.rrms.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tenants", uniqueConstraints = @UniqueConstraint(columnNames = "citizen_id"))
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(name = "citizen_id", nullable = false, unique = true, length = 12)
    private String citizenId;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String citizenId) { this.citizenId = citizenId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
