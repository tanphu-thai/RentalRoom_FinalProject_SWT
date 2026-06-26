# Lab execution guide — Rental Room Management System

## Before testing

1. Run the Spring Boot backend in IntelliJ.
2. Run the React frontend in VS Code.
3. Login as `admin / Admin@123`.
4. Use the preloaded data in the root README.

## Lab 1 — Manual test cases

- **GUI:** Login, Room List, Add/Edit Room, Tenant, Contract, Invoice pages.
- **Validation:** Duplicate `R101`, Citizen ID with fewer than 12 digits, negative deposit, negative/invalid meter readings.
- **Date/time:** Forgot Password displays a demo OTP. It is valid only while `currentTime < expiresAt`, which is a strict 5-minute rule.

## Helpful exact test data

| Scenario | Input |
|---|---|
| Duplicate Room ID | `R101` |
| Existing Citizen ID | `079123456789` |
| Invalid Citizen ID | `123456789` |
| Invalid phone | `12345` |
| Negative deposit | `-100000` |
| Vacant room | `R101` |
| Occupied room | `R201` |

## Unit test evidence

Run the tests under `backend/src/test/java`. They demonstrate the Arrange–Act–Assert format and Mockito repository mocks.

## Lab 2–4 note

This project includes real UI, REST API, business rules and JUnit/Mockito test seams. Upload the exact Lab 2–4 sheets/requirements before you write those submissions, because each instructor template can have different required sections.
