## Refactor `BacktestRepository` from JPA to Spring Data JDBC

This document describes the plan to migrate the `BacktestRepository` from Spring Data JPA (`JpaRepository`) to Spring Data JDBC with minimal impact on the existing API contract (DTOs, controllers, URLs, and JSON structures should remain unchanged).

---

### 1. Motivation and constraints

- **Motivation**
  - Simplify the persistence model (no lazy-loading, proxies, or JPA-specific features).
  - Make the data access layer closer to plain SQL while still leveraging Spring Data repositories.
  - Improve predictability of SQL and performance characteristics for read-heavy workloads.
- **Constraints**
  - Preserve the existing REST API behavior and payloads.
  - Preserve the existing MySQL schema (no changes to the `backtest` table).
  - Keep JSON columns (`config`, `backtest`, `performance`) as raw JSON strings mapped to `VARCHAR`/`JSON` columns.

---

### 2. Dependency and configuration changes

- **Gradle**
  - Replace `spring-boot-starter-data-jpa` with `spring-boot-starter-data-jdbc`.
  - Keep the MySQL driver dependency unchanged.
- **Configuration**
  - Remove JPA/Hibernate-specific properties (e.g. `spring.jpa.*`) from `application.properties`.
  - Ensure the standard Spring Data JDBC configuration is active (no additional dialect configuration is required).
  - Keep the datasource configuration (URL, username, password, driver) as-is.

---

### 3. Domain model changes for Spring Data JDBC

Spring Data JDBC uses a simpler mapping model than JPA. Adjust the `BacktestEntity` to a JDBC-oriented aggregate:

- **Annotations and semantics**
  - Replace `@Entity` with `@Table("backtest")`.
  - Use `@Id` on the `id` field.
  - Keep `@Column` annotations where column names differ from field names (`datefrom`, `dateto`, `confighash`) or where explicit definition is useful (JSON columns).
  - Remove JPA-specific annotations such as `@GeneratedValue(strategy = GenerationType.IDENTITY)` if not supported in the chosen Spring Data JDBC version; instead rely on database-generated IDs and Spring Data JDBC’s support for identity columns.
- **Fields**
  - Keep the same fields and Java types as specified in section 3 (including JSON columns mapped as `String`).
  - Ensure the class is treated as an aggregate root (no JPA relationships or proxies).

---

### 4. Repository migration (`BacktestRepository`)

- **Current state**
  - `BacktestRepository` extends `JpaRepository<BacktestEntity, Long>` with additional finder methods.
- **Target state (Spring Data JDBC)**
  - Change the base interface to a Spring Data JDBC–compatible repository, for example:
    - `PagingAndSortingRepository<BacktestEntity, Long>` (for paging support) or an equivalent Spring Data JDBC paging interface supported by the Spring Boot version in use.
  - Keep the existing finder method signatures as much as possible:
    - `Optional<BacktestEntity> findByMethodAndAssetAndCurrencyAndDateFromAndDateToAndConfigHash(...)`
    - `Page<BacktestEntity> findByMethod(String method, Pageable pageable);`
    - `Page<BacktestEntity> findByMethodAndAssetAndCurrency(String method, String asset, String currency, Pageable pageable);`
  - If the Spring Data JDBC version does not support `Page` directly, introduce one of the following strategies:
    - Implement custom repository methods that run count + limited queries using `JdbcTemplate` and manually construct `Page<BacktestEntity>`.
    - Or, if acceptable, relax strict `Page` usage to `Slice`/`List` on the service layer while keeping the API contract stable (e.g. custom page wrapper).

---

### 5. Service and controller adaptations

- **Service layer**
  - Keep `BacktestService` and `BacktestServiceImpl` interfaces and signatures unchanged.
  - Adapt internal calls to use the Spring Data JDBC–based `BacktestRepository`.
  - If repository paging semantics change (e.g. custom paging implementation), encapsulate that logic entirely inside the service layer so controllers and DTOs are unaffected.
- **Controller**
  - No changes expected to controller method signatures or endpoint URLs.
  - Ensure that pagination and filtering behavior (query parameters and default values) remain the same from the client perspective.

---

### 6. Testing and verification strategy for the migration

- **Unit and service tests**
  - Reuse existing service tests (`BacktestServiceImplTest`) to validate behavior against the new repository implementation.
  - Add tests to cover any new custom paging logic if `Page` needs to be constructed manually.
- **Repository/integration tests**
  - Add Spring Data JDBC–focused integration tests against MySQL (or Testcontainers) to:
    - Verify entity mapping to the `backtest` table.
    - Confirm that JSON columns are correctly persisted and read back as strings.
    - Confirm that finder methods return expected results with filters and sorting.
- **API regression tests**
  - Re-run controller tests (MockMvc) to ensure there are no changes in JSON responses, status codes, or error handling.

---

### 7. Step-by-step migration order

1. **Introduce Spring Data JDBC dependency and basic configuration**
   - Add `spring-boot-starter-data-jdbc` and remove `spring-boot-starter-data-jpa`.
   - Clean up JPA/Hibernate-specific properties while keeping datasource configuration.
2. **Refactor the domain model**
   - Update `BacktestEntity` annotations for Spring Data JDBC.
   - Ensure it compiles and maps correctly without JPA.
3. **Migrate `BacktestRepository`**
   - Change the base interface to a Spring Data JDBC repository (`PagingAndSortingRepository` or equivalent).
   - Adjust finder method signatures only if strictly required by Spring Data JDBC.
4. **Adjust service implementation if needed**
   - Update `BacktestServiceImpl` to work with the JDBC-based repository (especially for paging).
5. **Update and extend tests**
   - Fix any failing unit/service tests.
   - Add integration tests for JDBC mappings and queries.
6. **Run full regression suite**
   - Execute all tests (unit, integration, controller).
   - Manually test key endpoints (list, get by id, get by composite key) against a real database.
7. **Cleanup**
   - Remove any remaining unused JPA imports, annotations, or configuration.
   - Update project documentation to mention the use of Spring Data JDBC instead of JPA.

