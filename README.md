# Pivot Banking System - Customer Microservice (`pivot-ms-customer`)

## Overview
Reactive microservice responsible for managing **Bank Customers** (**Personal** and **Business** entities) and their Phase II banking profiles (**`STANDARD`**, **`VIP`**, and **`PYME`**). It enforces tax/identity document rules (`DNI`, `CEX`, `PASSPORT`, `RUC`), guarantees uniqueness via **SHA-256 Blind Indexing**, protects PII at rest with **AES-256-GCM encryption** backed by **HashiCorp Vault**, and manages logical lifecycle transitions (`ACTIVE`, `INACTIVE`, `BLOCKED`).

## Architecture, Design Patterns & Quality Gates
- **Architectural Style:** Hexagonal Architecture (Ports and Adapters) with strict separation of `domain`, `application`, and `infrastructure` layers.
- **Reactive Stack:** Java 17 + Spring Boot 4.1.2 + Spring WebFlux (Netty) + **RxJava 3** (`Single`, `Maybe`, `Flowable`, `Completable`) + Reactive Spring Data MongoDB + Netflix Eureka Client.
- **SOLID & Design Patterns (Zero God Classes):**
  - **Single Responsibility Principle (SRP):** Dedicated reactive use cases (`CreateCustomerUseCaseImpl`, `RetrieveCustomerUseCaseImpl`, `FindCustomerByIdUseCaseImpl`, `UpdateCustomerUseCaseImpl`, `DeleteCustomerUseCaseImpl`, `ReactivateCustomerUseCaseImpl`).
  - **Strategy Pattern (OCP):** `PersonalCustomerStrategy` (validates `DNI`/`CEX`/`PASSPORT`, natural person names, and `STANDARD`/`VIP` profiles) and `BusinessCustomerStrategy` (validates 11-digit `RUC`, company business name, and `STANDARD`/`PYME` profiles).
  - **Rich Enum Pattern:** `DocumentType` encapsulates regex format validation per identity document type.
- **Code Quality & Testing:**
  - **Google Java Style (`maven-checkstyle-plugin`):** Enforced during Maven `validate` phase.
  - **Unit Testing (`JUnit 5` + `Mockito` + `RxJava 3 TestObserver` + `StepVerifier`):** AAA pattern and FIRST principles achieving **>90% code coverage** verified by **JaCoCo** and **SonarQube**.

## Security & Secrets (`HashiCorp Vault`)
- **Vault Path:** `secret/pivot-bank`
- **Required Secret Key:** `aes-secret-key` (32-byte Base64 key used for AES-256-GCM encryption at rest and SHA-256 blind indexing).
- **Mask-on-Read Presentation:** Full `documentNumber` is returned **only once** upon creation (`POST /customers`). All subsequent queries mask the document number (`****5678`) via `DataMaskingUtil`.

## API Endpoints (`/api/v1/customers`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/customers` | Register a new `PERSONAL` (`STANDARD`/`VIP`) or `BUSINESS` (`STANDARD`/`PYME`) customer |
| `GET` | `/api/v1/customers` | Retrieve all registered customers (masked document numbers) |
| `GET` | `/api/v1/customers/{id}` | Retrieve customer by ID |
| `GET` | `/api/v1/customers/document/{documentNumber}` | Retrieve customer by document number via SHA-256 blind index |
| `PUT` | `/api/v1/customers/{id}` | Update customer profile, contact information, or status |
| `DELETE` | `/api/v1/customers/{id}` | Soft delete customer (logical transition to `status = INACTIVE`) |
| `PATCH` | `/api/v1/customers/{id}/reactivate` | Reactivate a logically deleted customer (`status = ACTIVE`) |

## Inter-Service Communication & Observability
- **`pivot-ms-config` (`:8888`):** Fetches externalized YAML configuration at startup.
- **`pivot-ms-eureka` (`:8761`):** Registers service instance as `MS-CUSTOMER` for dynamic discovery.
- **`pivot-ms-gateway` (`:8080`):** Routes external traffic and propagates `X-Correlation-Id`.
- **Consumed by:** `ms-account` (`:8082`), `ms-credit` (`:8083`), and `ms-transaction` (`:8084`) via non-blocking `WebClient` protected by **Resilience4j Circuit Breakers (2s timeout)**.
- **Observability:** Structured logs exported to `../ENVIROMENT/logs/ms-customer.log` and ingested by **Promtail -> Loki -> Grafana (`:3000`)**.

## Database (Database per Service Pattern)
- **Engine:** MongoDB 7.0 (Reactive Spring Data MongoDB using derived query methods, zero `@Query`).
- **Database Name:** `db_pivot_customer`
- **Collection:** `customers`

## How to Run (Local & Docker)
1. Ensure MongoDB (`:27017`), HashiCorp Vault (`:8200`), `ms-config` (`:8888`), and `ms-eureka` (`:8761`) are running.
2. Run unit tests, Checkstyle validation, and generate the JaCoCo coverage report:
   ```powershell
   mvn clean test checkstyle:check jacoco:report
   ```
3. Start the microservice locally:
   ```powershell
   mvn spring-boot:run
   ```
4. Build and run as an independent Docker container:
   ```powershell
   docker build -t pivot-ms-customer:2.0.0 .
   docker run -d -p 8081:8081 --name pivot-ms-customer pivot-ms-customer:2.0.0
   ```