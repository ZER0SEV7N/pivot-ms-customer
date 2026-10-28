# Pivot Banking System - Customer Microservice (`ms-customer`)

## Overview
Reactive microservice responsible for managing **Personal** and **Business** bank customers. It enforces domain identity rules, document uniqueness, and customer classification profiles (`STANDARD`, `VIP`, `PYME`) applying the **Strategy Design Pattern** and **SOLID Principles**.

## Architecture & Design Patterns
- **Architectural Style:** Hexagonal Architecture (Ports and Adapters) strictly separating `domain`, `application`, and `infrastructure` layers.
- **Reactive Engine:** Spring WebFlux (Netty non-blocking runtime) integrated with **RxJava 3** (`Single`, `Maybe`, `Flowable`, `Completable`).
- **Design Patterns Applied:**
  - **Strategy Pattern:** `PersonalCustomerStrategy` and `BusinessCustomerStrategy` for decoupling validation rules per customer type.
  - **Repository & Adapter Pattern:** `CustomerPersistenceAdapter` isolating MongoDB persistence from the domain model.
  - **Immutable Data Carrier Pattern:** Java 17 `record` types for Web DTOs.
- **Security & Observability:** Structured logging with **Logback** including PII data masking (`DataMaskingUtil`) for sensitive document numbers.

## Inter-Service Communication
- **Configuration Dependency:** Fetches externalized configuration properties at startup from **`ms-config`** (`http://localhost:8888`).
- **Consumers of this Microservice:**
  - **`ms-account`**: Queries customer existence, type (`PERSONAL` / `BUSINESS`), and profile before opening bank accounts.
  - **`ms-credit`**: Queries customer information before granting loans or credit cards.

## Database (Database per Service Pattern)
- **Engine:** MongoDB (Reactive Spring Data MongoDB without `@Query` or dynamic queries).
- **Database Name:** `db_pivot_customer`
- **Collection:** `customers` (with unique index on `documentNumber`).

## How to Run
1. Start the MongoDB container:
   ```bash
   docker compose up -d
    ```

2. Ensure **`ms-config`** is running on port `8888`.
3. Build and run **`ms-customer`**:
    ```bash
    mvn clean spring-boot:run
    ```


4. The service will start on port **`8081`**.
5. **API Testing:** Import the attached **`pivot-ms-customer.postman_collection.json`** file into Postman to execute all requests and business rule validations.

---
