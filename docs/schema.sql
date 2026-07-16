-- ============================================================
-- Rental Room Management System — Database Schema (SQL)
-- Được sinh từ JPA Entity definitions (Spring Boot / Hibernate)
-- DBMS: H2 (tương thích PostgreSQL mode)
-- Ngày tạo: 2026-07
-- ============================================================

-- ============================================================
-- BẢNG: rooms (Phòng trọ)
-- ============================================================
CREATE TABLE IF NOT EXISTS rooms (
    id         BIGINT          NOT NULL AUTO_INCREMENT,
    room_code  VARCHAR(50)     NOT NULL,
    room_type  VARCHAR(100)    NOT NULL,
    area       DECIMAL(10, 2)  NOT NULL,
    base_price DECIMAL(15, 2)  NOT NULL,
    status     VARCHAR(20)     NOT NULL, -- VACANT | OCCUPIED | MAINTENANCE

    CONSTRAINT pk_rooms        PRIMARY KEY (id),
    CONSTRAINT uq_rooms_code   UNIQUE (room_code)
);

-- ============================================================
-- BẢNG: tenants (Khách thuê)
-- ============================================================
CREATE TABLE IF NOT EXISTS tenants (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    full_name   VARCHAR(255) NOT NULL,
    citizen_id  VARCHAR(12)  NOT NULL,
    phone       VARCHAR(20)  NOT NULL,
    email       VARCHAR(255) NOT NULL,

    CONSTRAINT pk_tenants           PRIMARY KEY (id),
    CONSTRAINT uq_tenants_citizen   UNIQUE (citizen_id)
);

-- ============================================================
-- BẢNG: user_accounts (Tài khoản đăng nhập)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_accounts (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    username            VARCHAR(100) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    role                VARCHAR(20)  NOT NULL, -- ADMIN | TENANT
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    failed_login_count  INT          NOT NULL DEFAULT 0,
    locked_until        TIMESTAMP,
    tenant_id           BIGINT,               -- FK → tenants(id) | NULL nếu là ADMIN

    CONSTRAINT pk_user_accounts         PRIMARY KEY (id),
    CONSTRAINT uq_user_accounts_uname   UNIQUE (username),
    CONSTRAINT uq_user_accounts_email   UNIQUE (email),
    CONSTRAINT fk_user_accounts_tenant  FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- ============================================================
-- BẢNG: rental_contracts (Hợp đồng thuê phòng)
-- ============================================================
CREATE TABLE IF NOT EXISTS rental_contracts (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    room_id                     BIGINT          NOT NULL,
    tenant_id                   BIGINT          NOT NULL,
    deposit_amount              DECIMAL(15, 2)  NOT NULL,
    start_date                  DATE            NOT NULL,
    end_date                    DATE,
    initial_electricity_reading DECIMAL(12, 2)  NOT NULL,
    initial_water_reading       DECIMAL(12, 2)  NOT NULL,
    final_electricity_reading   DECIMAL(12, 2),
    final_water_reading         DECIMAL(12, 2),
    refundable_deposit          DECIMAL(15, 2),
    status                      VARCHAR(20)     NOT NULL, -- ACTIVE | TERMINATED
    created_at                  TIMESTAMP       NOT NULL,

    CONSTRAINT pk_rental_contracts          PRIMARY KEY (id),
    CONSTRAINT fk_rental_contracts_room     FOREIGN KEY (room_id)   REFERENCES rooms (id),
    CONSTRAINT fk_rental_contracts_tenant   FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- ============================================================
-- BẢNG: invoices (Hóa đơn hàng tháng)
-- ============================================================
CREATE TABLE IF NOT EXISTS invoices (
    id                           BIGINT          NOT NULL AUTO_INCREMENT,
    contract_id                  BIGINT          NOT NULL,
    billing_month                VARCHAR(7)      NOT NULL,  -- Định dạng: yyyy-MM
    previous_electricity_reading DECIMAL(12, 2)  NOT NULL,
    current_electricity_reading  DECIMAL(12, 2)  NOT NULL,
    previous_water_reading       DECIMAL(12, 2)  NOT NULL,
    current_water_reading        DECIMAL(12, 2)  NOT NULL,
    electricity_unit_price       DECIMAL(12, 2)  NOT NULL,
    water_unit_price             DECIMAL(12, 2)  NOT NULL,
    other_services               DECIMAL(12, 2)  NOT NULL,
    room_fee                     DECIMAL(15, 2)  NOT NULL,
    electricity_cost             DECIMAL(12, 2)  NOT NULL,
    water_cost                   DECIMAL(12, 2)  NOT NULL,
    total_amount                 DECIMAL(15, 2)  NOT NULL,
    status                       VARCHAR(20)     NOT NULL,  -- UNPAID | PAID | CANCELED
    paid_amount                  DECIMAL(15, 2),
    paid_at                      TIMESTAMP,
    created_at                   TIMESTAMP       NOT NULL,
    due_date                     TIMESTAMP,
    payment_date                 TIMESTAMP,

    CONSTRAINT pk_invoices                  PRIMARY KEY (id),
    CONSTRAINT uq_invoices_month            UNIQUE (contract_id, billing_month),
    CONSTRAINT fk_invoices_contract         FOREIGN KEY (contract_id) REFERENCES rental_contracts (id)
);

-- ============================================================
-- BẢNG: password_reset_tokens (OTP đặt lại mật khẩu)
-- ============================================================
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    code        VARCHAR(6)   NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_password_reset_tokens         PRIMARY KEY (id),
    CONSTRAINT fk_password_reset_tokens_user    FOREIGN KEY (user_id) REFERENCES user_accounts (id)
);
