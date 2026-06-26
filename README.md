# Rental Room Management System — Lab-Ready Full-Stack Project

A runnable academic project built from the submitted **Rental Room Management System SRS** and **Test Plan**.

- **Backend:** Java 17, Spring Boot, Spring Data JPA, H2 by default, JUnit 5 + Mockito
- **Frontend:** React + Vite
- **IDE:** Open `backend` in IntelliJ IDEA. Open `frontend` in Visual Studio Code.
- **Purpose:** Execute manual UI/validation/date-time test cases for Lab 1 and provide service-level code/tests for later SWT labs.

## What is implemented

- Authentication: login, failed-login lockout, logout, forgot password, OTP reset (5-minute strict expiry)
- Room CRUD: list/search/filter/add/edit/delete, duplicate code validation, area/base price validation
- Tenant CRUD: Citizen ID validation (12 digits), duplicate ID validation, phone validation
- Rental contract: vacant-room rule, non-negative deposit/readings, room status transition, termination
- Billing: meter reading validation, invoice calculation, edit only when unpaid, payment/cancel, revenue report
- Tenant portal: own contract and own invoices only
- Seed data, API error messages, JUnit 5/Mockito tests

## Quick start

### 1) Backend in IntelliJ

1. Open the `backend` folder as a Maven project in IntelliJ IDEA.
2. Select **JDK 17+**.
3. Wait for Maven dependencies to download.
4. Run `com.rrms.RrmsApplication`.
5. Backend runs at `http://localhost:8080`.
6. H2 console: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:file:./data/rrms;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH`
   - User: `sa`
   - Password: empty

### 2) Frontend in VS Code

```bash
cd frontend
npm install
npm run dev
```

Open the Vite URL, normally `http://localhost:5173`.

## Demo accounts and data

| Role | Username | Password | Email |
|---|---|---|---|
| Admin | `admin` | `Admin@123` | `admin@rrms.local` |
| Tenant | `tenant1` | `Tenant@123` | `tenant1@rrms.local` |

Seed rooms:
- `R101` — Vacant (use it to create a contract)
- `R102` — Maintenance
- `R201` — Occupied (has a seeded tenant contract/invoice)

Existing tenant Citizen ID: `079123456789`

## Manual test evidence

For Lab 1, run the UI in the browser, execute each row in your Excel test case, then update **Result**, **Test Date**, and **Test Report**. See `docs/LAB_EXECUTION_GUIDE.md`.

## Unit tests

In IntelliJ: right-click `src/test/java` → Run tests.

Or terminal:

```bash
mvn test
```

## Important note

The Forgot Password screen displays the OTP in the UI **only because this is an academic local demo project**. Never expose OTP values in a production application.
