# Pivot Banking System - Customer Microservice (`pivot-ms-customer`)

## Overview
Reactive microservice responsible for managing **Personal** and **Business** bank customers. It enforces domain identity rules, strict Peruvian document format validations (`DNI`, `RUC`, `CE`, `PASSPORT`), document uniqueness, customer classification profiles (`STANDARD`, `VIP`, `PYME`), and logical lifecycle states (`ACTIVE`, `BLOCKED`, `INACTIVE`).

## Architecture, Design Patterns & Security
- **Architectural Style:** Hexagonal Architecture (Ports and Adapters) with strict separation of `domain`, `application`, and `infrastructure` layers.
- **Reactive Stack:** Spring Boot 4 + Spring WebFlux (Netty) + **RxJava 3** (`Single`, `Maybe`, `Flowable`, `Completable`).
- **SOLID & Design Patterns:**
  - **Single Responsibility Principle (SRP):** Dedicated Use Case implementations (`CreateCustomerUseCaseImpl`, `RetrieveCustomerUseCaseImpl`, `UpdateCustomerUseCaseImpl`, `DeleteCustomerUseCaseImpl`, `ReactivateCustomerUseCaseImpl`).
  - **Strategy Pattern (OCP):** `PersonalCustomerStrategy` (validates natural person identity fields and `STANDARD`/`VIP` profiles) and `BusinessCustomerStrategy` (enforces `RUC` document type, `businessName`, and `STANDARD`/`PYME` profiles).
  - **Rich Enum Pattern (`DocumentType`):** Encapsulates pre-compiled regular expressions (`Pattern.compile`) to validate exact format and length for `DNI` (8 digits), `RUC` (11 digits starting with `10` or `20`), `CE`, and `PASSPORT`.
  - **Immutable DTOs:** Java 17 `record` types across all web payloads.
- **Banking-Grade Security:**
  - **PII Data Masking:** Structured logging with **Logback** and `DataMaskingUtil` to mask sensitive identity document numbers (`****5678`) in console and application logs.
  - **Soft Delete & Reactivation:** `DELETE /customers/{id}` performs a logical deactivation (`status = INACTIVE`) to preserve historical banking audit trails. Inactive or blocked customers can be restored via `PATCH /customers/{id}/reactivate`.

## API Endpoints (`/api/v1/customers`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/customers` | Register a new Personal or Business customer |
| `GET` | `/api/v1/customers` | Retrieve all registered customers |
| `GET` | `/api/v1/customers/{id}` | Retrieve customer by unique ID |
| `GET` | `/api/v1/customers/document/{documentNumber}` | Retrieve customer by identity document number (`DNI`, `RUC`, etc.) |
| `PUT` | `/api/v1/customers/{id}` | Update customer information and profile |
| `DELETE` | `/api/v1/customers/{id}` | Soft delete customer (`status = INACTIVE`) |
| `PATCH` | `/api/v1/customers/{id}/reactivate` | Reactivate an `INACTIVE` or `BLOCKED` customer back to `ACTIVE` |

## Inter-Service Communication
- **`pivot-ms-config` (`:8888`):** Fetches externalized configuration properties at startup.
- **Consumers of this Microservice:**
  - **`pivot-ms-account` (`:8082`):** Queries `GET /api/v1/customers/{id}` via reactive `WebClient` to verify customer existence, active status, and type (`PERSONAL` / `BUSINESS`) before opening bank accounts.
  - **`pivot-ms-credit` (`:8083`):** Queries `GET /api/v1/customers/{id}` via reactive `WebClient` before granting loans or issuing credit cards.

## Database (Database per Service Pattern)
- **Engine:** MongoDB 7 (Reactive Spring Data MongoDB using derived query methods, zero `@Query`).
- **Database Name:** `db_pivot_customer`
- **Collection:** `customers` (with unique index on `documentNumber`).

## How to Run
1. Ensure the MongoDB container (`:27017`) and `ms-config` (`:8888`) are running from the `ENVIRONMENT` setup.
2. Start the microservice:
```powershell
   mvn clean spring-boot:run
```
3. The service will start on port *8081*.
4. API Testing: Import *pivot-ms-customer.postman_collection.json* into Postman to run all test scenarios.

---