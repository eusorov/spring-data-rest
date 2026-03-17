## Backtest API Requirements & Technical Plan

### 1. Overview

**Goal:** Add a set of versioned REST API endpoints to expose data from the MySQL database `cryptodb`, table `backtest`, using a clean layered architecture and Spring Data JPA.

**Key capabilities:**
- List backtests with pagination and basic filtering.
- Fetch a single backtest by primary key (`id`).
- Fetch a single backtest by its unique composite business key.
- Return JSON columns as raw JSON strings to keep the implementation simple and flexible.

**Table schema:**

```sql
CREATE TABLE `backtest` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `method` varchar(20) NOT NULL,
  `asset` varchar(5) NOT NULL,
  `currency` varchar(5) NOT NULL,
  `datefrom` int unsigned NOT NULL,
  `dateto` int unsigned NOT NULL,
  `confighash` varchar(255) NOT NULL,
  `config` json NOT NULL,
  `backtest` json NOT NULL,
  `performance` json NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `method` (`method`,`asset`,`currency`,`datefrom`,`dateto`,`confighash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

**Main trade-off:** JSON columns (`config`, `backtest`, `performance`) are represented as raw JSON strings in the entity and DTOs. Clients are responsible for parsing/validating their contents. This keeps the backend implementation simple and avoids tightly coupling to an inner JSON schema.

---

### 2. Dependencies and configuration

#### 2.1 Gradle (`build.gradle`)

Add/ensure the following dependencies:

- **Web & REST**
  - `implementation "org.springframework.boot:spring-boot-starter-web"`
- **Data access**
  - `implementation "org.springframework.boot:spring-boot-starter-data-jpa"`
  - `implementation "mysql:mysql-connector-j:<current_version>"`
- **Testing**
  - Use existing Spring Boot test dependencies; extend as needed for new tests.

#### 2.2 Application properties (`src/main/resources/application.properties`)

Configure a MySQL datasource for `cryptodb`:

- **Datasource**
  - `spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/cryptodb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
  - `spring.datasource.username=${DB_USERNAME:cryptouser}`
  - `spring.datasource.password=${DB_PASSWORD:changeme}`
  - `spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver`
- **JPA / Hibernate**
  - `spring.jpa.hibernate.ddl-auto=none` (table already exists)
  - `spring.jpa.show-sql=false`
  - `spring.jpa.properties.hibernate.format_sql=true`
  - `spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect`

Optionally enable SQL logging in development via logging properties.

---

### 3. JPA entity for `backtest`

#### 3.1 Location and naming

- **Package:** `com.example.spring_data_rest.backtest.domain`
- **File:** `BacktestEntity.java`

#### 3.2 Entity definition

- Annotate class with:
  - `@Entity`
  - `@Table(name = "backtest", uniqueConstraints = @UniqueConstraint(name = "uk_backtest_method_asset_currency_date_confighash", columnNames = { "method", "asset", "currency", "datefrom", "dateto", "confighash" }))`
- Fields:
  - `Long id`
    - `@Id`
    - `@GeneratedValue(strategy = GenerationType.IDENTITY)`
  - `String method`
    - `@Column(nullable = false, length = 20)`
  - `String asset`
    - `@Column(nullable = false, length = 5)`
  - `String currency`
    - `@Column(nullable = false, length = 5)`
  - `Long dateFrom`
    - `@Column(name = "datefrom", nullable = false)`
  - `Long dateTo`
    - `@Column(name = "dateto", nullable = false)`
  - `String configHash`
    - `@Column(name = "confighash", nullable = false, length = 255)`
  - `String config`
    - `@Column(columnDefinition = "json", nullable = false)`
  - `String backtest`
    - `@Column(columnDefinition = "json", nullable = false)`
  - `String performance`
    - `@Column(columnDefinition = "json", nullable = false)`

The entity focuses only on persistence concerns and does not contain business logic.

---

### 4. DTOs and mapping

#### 4.1 DTO package

- **Package:** `com.example.spring_data_rest.backtest.api.dto`

#### 4.2 DTOs

- **`BacktestListItemDto`**
  - Fields:
    - `Long id`
    - `String method`
    - `String asset`
    - `String currency`
    - `Long dateFrom`
    - `Long dateTo`
    - `String configHash`
  - Purpose: lightweight representation for list endpoints (no JSON blobs to keep payload small).

- **`BacktestResponseDto`**
  - Fields:
    - `Long id`
    - `String method`
    - `String asset`
    - `String currency`
    - `Long dateFrom`
    - `Long dateTo`
    - `String configHash`
    - `String configJson`
    - `String backtestJson`
    - `String performanceJson`
  - Purpose: full representation for single-backtest endpoints, including JSON content as strings.

- **(Optional) `BacktestUniqueKeyRequestDto`**
  - Fields:
    - `String method`
    - `String asset`
    - `String currency`
    - `Long dateFrom`
    - `Long dateTo`
    - `String configHash`
  - Purpose: request body for a POST-based lookup by unique business key (optional; the primary lookup will be via query parameters on a GET endpoint).

#### 4.3 Mapper

- **Package:** `com.example.spring_data_rest.backtest.api`
- **File:** `BacktestMapper.java`
- Responsibilities:
  - `BacktestResponseDto toResponseDto(BacktestEntity entity)`
  - `BacktestListItemDto toListItemDto(BacktestEntity entity)`
- Approach:
  - Start with a manual mapping class (simple static or instance methods).
  - Can be replaced with MapStruct or another mapping library later if desired.

---

### 5. Repository layer

#### 5.1 Repository interface

- **Package:** `com.example.spring_data_rest.backtest.domain`
- **File:** `BacktestRepository.java`
- Definition:
  - `public interface BacktestRepository extends JpaRepository<BacktestEntity, Long>`

#### 5.2 Custom query methods

Add methods for key operations:

- Lookup by composite business key:
  - `Optional<BacktestEntity> findByMethodAndAssetAndCurrencyAndDateFromAndDateToAndConfigHash(String method, String asset, String currency, Long dateFrom, Long dateTo, String configHash);`
- Optional filtering for listing:
  - `Page<BacktestEntity> findByMethod(String method, Pageable pageable);`
  - `Page<BacktestEntity> findByMethodAndAssetAndCurrency(String method, String asset, String currency, Pageable pageable);`

Initially, listing can fall back to `findAll(Pageable pageable)` when no filters are provided.

---

### 6. Service layer

#### 6.1 Service interface

- **Package:** `com.example.spring_data_rest.backtest.service`
- **File:** `BacktestService.java`
- Methods:
  - `Page<BacktestListItemDto> listBacktests(String method, String asset, String currency, Long dateFrom, Long dateTo, Pageable pageable);`
  - `BacktestResponseDto getById(Long id);`
  - `BacktestResponseDto getByUniqueKey(String method, String asset, String currency, Long dateFrom, Long dateTo, String configHash);`

#### 6.2 Service implementation

- **File:** `BacktestServiceImpl.java`
- Annotate with `@Service`.
- Inject:
  - `BacktestRepository`
  - `BacktestMapper`
- Behavior:
  - **`listBacktests`**
    - Build `Pageable` from controller parameters.
    - If no filters are provided:
      - Use `backtestRepository.findAll(pageable)`.
    - If filters are provided (e.g. `method`, `asset`, `currency`):
      - Choose appropriate repository methods (`findByMethod`, `findByMethodAndAssetAndCurrency`, etc.).
    - Map `BacktestEntity` page to `Page<BacktestListItemDto>`.
  - **`getById`**
    - Use `backtestRepository.findById(id)`.
    - If missing, throw `BacktestNotFoundException`.
    - Map entity to `BacktestResponseDto`.
  - **`getByUniqueKey`**
    - Use `backtestRepository.findByMethodAndAssetAndCurrencyAndDateFromAndDateToAndConfigHash(...)`.
    - If missing, throw `BacktestNotFoundException`.
    - Map entity to `BacktestResponseDto`.

---

### 7. Exceptions and global error handling

#### 7.1 Domain-specific exception

- **Package:** `com.example.spring_data_rest.backtest.exception`
- **File:** `BacktestNotFoundException.java`
- Implementation:
  - Extend `RuntimeException`.
  - Provide constructors taking a message (and optionally key fields).

#### 7.2 Global exception handler

- **Package:** `com.example.spring_data_rest.common`
- **File:** `GlobalExceptionHandler.java` (create if none exists)
- Annotate with `@RestControllerAdvice`.
- Handlers:
  - `@ExceptionHandler(BacktestNotFoundException.class)`
    - Return HTTP 404 with JSON body, e.g.:
      - `timestamp`
      - `status`
      - `error`
      - `message`
      - `path`
  - Optionally handle:
    - Validation-related exceptions for 400 responses.
    - Generic `Exception` for 500 responses with safe messages.

---

### 8. REST API design

#### 8.1 Base path and versioning

- Base path: `/api/v1/backtests`
- All endpoints are read-only for this table (no create/update/delete).

#### 8.2 Endpoints

1. **List backtests (paginated, filterable)**
   - **Method:** `GET`
   - **Path:** `/api/v1/backtests`
   - **Query params:**
     - `page` (optional, default `0`)
     - `size` (optional, default `20`)
     - `sort` (optional, default `id,desc`)
     - Optional filters:
       - `method`
       - `asset`
       - `currency`
       - `dateFrom`
       - `dateTo`
   - **Response:**
     - `Page<BacktestListItemDto>` or equivalent JSON page wrapper.
   - **Behavior:**
     - Delegates to `BacktestService.listBacktests(...)`.

2. **Get backtest by ID**
   - **Method:** `GET`
   - **Path:** `/api/v1/backtests/{id}`
   - **Path variable:**
     - `id` (Long)
   - **Response:**
     - `BacktestResponseDto` if found.
     - 404 with error body if not found.
   - **Behavior:**
     - Delegates to `BacktestService.getById(id)`.

3. **Get backtest by unique composite key (via query params)**
   - **Method:** `GET`
   - **Path:** `/api/v1/backtests/by-key`
   - **Query params (all required):**
     - `method`
     - `asset`
     - `currency`
     - `dateFrom`
     - `dateTo`
     - `configHash`
   - **Response:**
     - `BacktestResponseDto` if found.
     - 404 if no record matches.
   - **Behavior:**
     - Validate presence of all required query params.
     - Delegate to `BacktestService.getByUniqueKey(...)`.

4. **(Optional) Get backtest by unique key via POST body**
   - **Method:** `POST`
   - **Path:** `/api/v1/backtests/by-key`
   - **Request body:**
     - `BacktestUniqueKeyRequestDto`
   - **Response:**
     - Same as the GET `/by-key` endpoint.
   - **Purpose:**
     - Alternative for clients that prefer sending the composite key in the body.

#### 8.3 Controller

- **Package:** `com.example.spring_data_rest.backtest.api`
- **File:** `BacktestController.java`
- Annotations:
  - `@RestController`
  - `@RequestMapping("/api/v1/backtests")`
- Responsibilities:
  - Accept/validate request parameters.
  - Build `Pageable` object for listing.
  - Call `BacktestService` methods and return DTOs.
  - Rely on global exception handling for errors.

---

### 9. Validation

- Use query parameter validation in controller:
  - `/by-key` GET endpoint:
    - All key parameters (`method`, `asset`, `currency`, `dateFrom`, `dateTo`, `configHash`) are required.
    - If any are missing or invalid, return 400 Bad Request with a clear message.
- If POST-based DTOs are used:
  - Annotate request DTO fields with `@NotBlank` / `@NotNull`.
  - Use `@Valid` in controller method parameters.
- Pagination:
  - Enforce reasonable maximum `size` to avoid large responses.

---

### 10. Kubernetes and environment wiring

- **Secrets (`k8s/local/secret-db.example.yaml`, `k8s/local/secret-db.yaml`):**
  - Ensure they contain:
    - `DB_USERNAME`
    - `DB_PASSWORD`
    - Optionally `DB_HOST`, `DB_PORT`
- **Deployment (`k8s/local/deployment.yaml`):**
  - Add environment variables on the app container:
    - `DB_USERNAME` (from secret)
    - `DB_PASSWORD` (from secret)
    - `DB_HOST` (default MySQL service name, e.g. `mysql`)
    - `DB_PORT` (default `3306`)
  - These feed into the placeholders used in `application.properties`.

No changes are expected for `service.yaml` and `namespace.yaml` beyond what’s already present for the application.

---

### 11. Testing strategy

#### 11.1 Unit tests

- **Mapper tests**
  - `BacktestMapperTest`:
    - Verify correct mapping from `BacktestEntity` to `BacktestResponseDto` and `BacktestListItemDto`.
- **Service tests**
  - `BacktestServiceImplTest` with mocked `BacktestRepository`:
    - `listBacktests`:
      - Should return mapped `Page<BacktestListItemDto>` when data exists.
      - Should handle empty results.
      - Should delegate to specific repository methods based on filters.
    - `getById`:
      - Happy path returns DTO.
      - Missing entity throws `BacktestNotFoundException`.
    - `getByUniqueKey`:
      - Happy path returns DTO.
      - Missing entity throws `BacktestNotFoundException`.

#### 11.2 Controller / web tests

- Use `MockMvc` with `@WebMvcTest(BacktestController.class)` or `@SpringBootTest`:
  - `GET /api/v1/backtests`:
    - Default pagination.
    - With filters.
  - `GET /api/v1/backtests/{id}`:
    - 200 and correct JSON for existing ID.
    - 404 for non-existing ID.
  - `GET /api/v1/backtests/by-key`:
    - 200 for valid composite key.
    - 404 when no record found.
    - 400 when required params are missing.

#### 11.3 Repository / integration tests (optional)

- Use H2 or Testcontainers MySQL to verify:
  - JPA mapping to `backtest` table.
  - JSON fields persisted and read correctly as strings.

---

### 12. Documentation

- Update project documentation (e.g. `README.md`) with a section:
  - **“Backtest API (v1)”**
  - Include:
    - Base path: `/api/v1/backtests`
    - List of endpoints and their purpose.
    - Query parameters and JSON field meanings.
    - Example requests/responses for:
      - Listing backtests.
      - Fetching by `id`.
      - Fetching by unique key.
    - Note that `configJson`, `backtestJson`, and `performanceJson` are raw JSON strings that clients must parse.

---

### 13. Implementation order / dependencies

Recommended implementation order:

1. **Dependencies & configuration**
   - Update `build.gradle`.
   - Configure datasource and JPA in `application.properties`.
2. **Domain model**
   - Implement `BacktestEntity`.
3. **DTOs and mapper**
   - Implement `BacktestListItemDto`, `BacktestResponseDto`, optional `BacktestUniqueKeyRequestDto`.
   - Implement `BacktestMapper`.
4. **Repository**
   - Implement `BacktestRepository` with CRUD and custom queries.
5. **Service layer**
   - Implement `BacktestService` and `BacktestServiceImpl`.
6. **Exceptions and global handler**
   - Implement `BacktestNotFoundException` and `GlobalExceptionHandler`.
7. **REST controller**
   - Implement `BacktestController` with endpoints under `/api/v1/backtests`.
8. **Kubernetes/env wiring**
   - Update DB-related secrets and deployment env variables.
9. **Tests**
   - Add unit, controller, and optional repository tests.
10. **Documentation**
   - Update README/API docs with endpoint details and examples.