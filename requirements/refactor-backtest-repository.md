## Refactor `BacktestRepository` from JPA to MyBatis

This document describes the plan to migrate the `BacktestRepository` from Spring Data JPA (`JpaRepository`) to a MyBatis-based repository with minimal impact on the existing API contract (DTOs, controllers, URLs, and JSON structures should remain unchanged).

---

### 1. Motivation and constraints

- **Motivation**
  - Simplify the persistence model (no lazy-loading, proxies, or JPA-specific features).
  - Make the data access layer explicitly SQL-driven using MyBatis mappers.
  - Improve predictability of SQL and performance characteristics for read-heavy workloads.
- **Constraints**
  - Preserve the existing REST API behavior and payloads.
  - Preserve the existing MySQL schema (no changes to the `backtest` table).
  - Keep JSON columns (`config`, `backtest`, `performance`) as raw JSON strings mapped to `VARCHAR`/`JSON` columns.

---

### 2. Dependency and configuration changes

- **Gradle**
  - Remove `spring-boot-starter-data-jpa`.
  - Add the MyBatis Spring Boot starter, for example `mybatis-spring-boot-starter`.
  - Keep the MySQL driver dependency unchanged.
- **Configuration**
  - Remove JPA/Hibernate-specific properties (e.g. `spring.jpa.*`) from `application.properties`.
  - Configure MyBatis mapper scanning (e.g. `@MapperScan("com.example.spring_data_rest.backtest.domain")`) and, if needed, `mybatis.*` properties for type aliases and mapper locations.
  - Keep the datasource configuration (URL, username, password, driver) as-is.

---

### 3. Domain model changes for MyBatis

MyBatis works well with simple POJOs. Adjust the `BacktestEntity` to be a plain Java object, leaving table and column mapping concerns to SQL:

- **Annotations and semantics**
  - Remove JPA-specific annotations such as `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, and `@Column`.
  - Keep `BacktestEntity` as a simple POJO whose field names either match column names or are explicitly mapped in SQL.
- **Fields**
  - Keep the same fields and Java types as in the current entity (including JSON columns mapped as `String`).
  - Ensure any naming differences between fields and columns (e.g. `dateFrom` vs `datefrom`) are handled in SQL using column aliases (e.g. `datefrom AS dateFrom`) rather than via annotations.

---

### 4. Repository migration (`BacktestRepository` → MyBatis mapper)

- **Current state**
  - `BacktestRepository` extends `JpaRepository<BacktestEntity, Long>` with additional finder methods.
- **Target state (MyBatis mapper)**
  - Convert `BacktestRepository` into a MyBatis mapper interface, annotated with `@Mapper`.
  - Define explicit SQL for each method using MyBatis annotations (or XML mappers, if preferred). For example, the simplest `findById` should look like:

    ```java
    @Select("SELECT * FROM backtest WHERE id = #{id}")
    Optional<BacktestEntity> findById(Long id);
    ```

  - Replace existing derived query methods with explicit SQL that preserves current semantics:
    - `Optional<BacktestEntity> findByMethodAndAssetAndCurrencyAndDateFromAndDateToAndConfigHash(...)` → a `SELECT ... FROM backtest WHERE method = #{method} AND asset = #{asset} AND currency = #{currency} AND datefrom = #{dateFrom} AND dateto = #{dateTo} AND confighash = #{configHash}` with proper column aliases where needed.
    - Methods currently returning `Page<BacktestEntity>` (e.g. `findByMethod`, `findByMethodAndAssetAndCurrency`) will be backed by:
      - A `List<BacktestEntity>` mapper method with `LIMIT` / `OFFSET` parameters for page content.
      - A corresponding `long countBy...(...)` method for total elements.
  - The service layer will wrap `List<BacktestEntity>` + count in `PageImpl<BacktestEntity>` to keep the public API returning `Page<BacktestEntity>`.

---

### 5. Service and controller adaptations

- **Service layer**
  - Keep `BacktestService` and `BacktestServiceImpl` interfaces and signatures unchanged.
  - Inject the MyBatis-based `BacktestRepository` mapper instead of the JPA repository.
  - Implement paging by:
    - Calling the mapper’s `List<BacktestEntity>` methods with calculated `LIMIT` and `OFFSET` based on `Pageable`.
    - Calling the corresponding `countBy...` methods.
    - Constructing `PageImpl<BacktestEntity>` using `PageRequest` and returning it to controllers, preserving existing behavior.
- **Controller**
  - No changes expected to controller method signatures or endpoint URLs.
  - Ensure that pagination and filtering behavior (query parameters and default values) remain the same from the client perspective.

---

### 6. Testing and verification strategy for the migration

- **Unit and service tests**
  - Reuse existing service tests (`BacktestServiceImplTest`) to validate behavior against the new MyBatis-based repository.
  - Add tests to cover any new custom paging logic where `PageImpl` is constructed in the service layer.
- **Repository/integration tests**
  - Add MyBatis-focused integration tests against MySQL (or Testcontainers) to:
    - Verify that `BacktestEntity` correctly maps to the `backtest` table via MyBatis result mapping.
    - Confirm that JSON columns are correctly persisted and read back as strings.
    - Confirm that finder methods return expected results with filters and sorting.
- **API regression tests**
  - Re-run controller tests (MockMvc) to ensure there are no changes in JSON responses, status codes, or error handling.

---

### 7. Step-by-step migration order

1. **Introduce MyBatis dependency and basic configuration**
   - Add `mybatis-spring-boot-starter` and remove `spring-boot-starter-data-jpa`.
   - Clean up JPA/Hibernate-specific properties while keeping datasource configuration.
   - Configure MyBatis mapper scanning and (optionally) XML locations.
2. **Refactor the domain model**
   - Strip JPA-specific annotations from `BacktestEntity`, keeping it as a plain POJO.
   - Ensure the class compiles and field names are suitable for SQL-based mapping (or provide column aliases in SQL).
3. **Migrate `BacktestRepository` to a MyBatis mapper**
   - Convert `BacktestRepository` into an `@Mapper` interface.
   - Introduce the basic `findById` method using an annotation-based query:

     ```java
     @Select("SELECT * FROM backtest WHERE id = #{id}")
     Optional<BacktestEntity> findById(Long id);
     ```

   - Add remaining finder methods with explicit SQL (and `countBy...` variants for paging).
4. **Adjust service implementation**
   - Update `BacktestServiceImpl` to call mapper methods instead of JPA repository methods.
   - Implement `PageImpl` construction around mapper results to preserve existing paging behavior.
5. **Update and extend tests**
   - Fix any failing unit/service tests.
   - Add integration tests for MyBatis mappings and queries.
6. **Run full regression suite**
   - Execute all tests (unit, integration, controller).
   - Manually test key endpoints (list, get by id, get by composite key) against a real database.
7. **Cleanup**
   - Remove any remaining unused JPA imports, annotations, or configuration.
   - Update project documentation to mention the use of MyBatis instead of JPA.