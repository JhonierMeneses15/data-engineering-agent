# TEST-002 — Plan

## Plan Version: 2.0

This plan is derived from the approved specification (`spec.md`) and defines the implementation tasks, test design, and convergence criteria. It accounts for existing code that partially implements requirements but needs alignment with the spec.

---

## 1. Gap Analysis: Current State vs. Spec

| Spec Requirement | Current State | Action Required |
|------------------|---------------|-----------------|
| **IcebergConfig case classes** (catalog, table, write) | Only `tableName`, `warehousePath` | Replace with full nested case classes per spec §Configuration Case Classes |
| **HOCON config** (`application.conf`) | Minimal `iceberg { tableName, warehousePath }` | Replace with full structure per spec §application.conf Structure |
| **IcebergWriter API** | Old `.write.format("iceberg").option(...)` | Migrate to `writeTo(...).using("iceberg").partitionedBy(...).createOrReplace()` |
| **Partitioning** | `.option("partitioning", "event_date")` | `.partitionedBy($"event_date")` (Column API) |
| **Create/Append semantics** | `.mode("append")` (creates on first write) | `.createOrReplace()` for idempotent create-or-replace, or check table existence |
| **Catalog configuration** | Not configured | Configure via `spark.sql.catalog.<name>` properties from config |
| **PureConfig loading** | Not used in Main (hardcoded) | Load `IcebergConfig` from HOCON in Main |
| **Iceberg dependency** | Missing from pom.xml | Add `iceberg-spark-runtime-3.5_2.12:1.6.1` |
| **Main.scala delegation** | Delegates but hardcoded config | Load config via PureConfig, then delegate |

---

## 2. Implementation Tasks

### Task 1: Update Iceberg Configuration Case Classes
- **Description**: Replace `IcebergConfig.scala` with the full nested case class hierarchy from spec.
- **Location**: `src/main/scala/com/example/dataengineering/config/IcebergConfig.scala`
- **Deliverable**: 
  - `IcebergCatalogConfig(type: String, warehouse: String, properties: Map[String, String] = Map.empty)`
  - `IcebergTableConfig(identifier: String)`
  - `IcebergWriteConfig(format: String = "parquet", targetFileSizeBytes: Long = 134217728L)`
  - `IcebergConfig(catalog: IcebergCatalogConfig, table: IcebergTableConfig, write: IcebergWriteConfig)`
- **PureConfig annotations**: Use `@ConfigName` for kebab-case mapping (`target-file-size-bytes`)

### Task 2: Update HOCON Configuration File
- **Description**: Replace `iceberg-config.conf` with full structure from spec.
- **Location**: `src/main/resources/iceberg-config.conf` (or `application.conf` per convention)
- **Content**: Full `iceberg { catalog { type, warehouse, properties }, table { identifier }, write { format, target-file-size-bytes } }` block

### Task 3: Add Iceberg Dependency to pom.xml
- **Description**: Add `iceberg-spark-runtime-3.5_2.12` dependency (version 1.6.1 per spec).
- **Location**: `pom.xml` → `<dependencies>`
- **Constraint**: Only modify pom.xml for this justified dependency addition.

### Task 4: Rewrite IcebergWriter to Use writeTo API
- **Description**: Replace current implementation with spec-compliant `writeTo` API.
- **Location**: `src/main/scala/com/example/dataengineering/IcebergWriter.scala`
- **Implementation**:
  - Configure catalog properties on SparkSession: `spark.sql.catalog.<catalogName>.*`
  - Use `df.writeTo(config.table.identifier).using("iceberg")`
  - Apply `.partitionedBy(functions.col("event_date"))`
  - Apply table properties from `config.write` (format, target-file-size-bytes)
  - Use `.createOrReplace()` for create-if-not-exists + replace semantics, or detect existence and use `.append()`
  - **Important**: Per spec FR-003 (append without overwrite), use `.createOrReplace()` only on first write, then `.append()` — or use `.createOrReplace()` which replaces but we need append. Best: check if table exists via Spark catalog, then branch.
- **Interface**: Keep `trait IcebergWriter { def write(df: DataFrame, config: IcebergConfig)(implicit spark: SparkSession): Unit }` and `object IcebergWriter { def apply(): IcebergWriter = new IcebergWriterImpl() }` per spec §Interface Specification.

### Task 5: Update Main.scala to Load Config via PureConfig
- **Description**: Remove hardcoded config, load from HOCON using PureConfig.
- **Location**: `src/main/scala/com/example/dataengineering/Main.scala`
- **Changes**:
  - Import `com.github.pureconfig.ConfigSource`
  - Load `IcebergConfig` from `ConfigSource.resources("iceberg-config.conf").loadOrThrow[IcebergConfig]`
  - Configure SparkSession catalog properties from `config.catalog` before writing
  - Delegate to `IcebergWriter.write(df, config)`
  - Keep try/finally with local `import spark.implicits._`

### Task 6: Update Unit Tests for New API and Config
- **Description**: Rewrite `IcebergWriterTest.scala` to test new `writeTo` API and config structure.
- **Location**: `src/test/scala/com/example/dataengineering/IcebergWriterTest.scala`
- **Test Cases**:
  - Write creates table with correct schema and partitioning when not exists
  - Second write appends without overwriting (verify count increases)
  - Schema preservation (read back matches input schema)
  - Config parsing from HOCON (PureConfig)
  - Catalog properties applied to SparkSession

### Task 7: Update Integration Test
- **Description**: Rewrite `IcebergIntegrationTest.scala` for new config and API.
- **Location**: `src/test/scala/com/example/dataengineering/IcebergIntegrationTest.scala`
- **Test Cases**:
  - End-to-end: create table, verify partitioning by event_date
  - Append second batch, verify count and data integrity
  - Verify table identifier and warehouse from config

---

## 3. Test Design

### Unit Test Pattern
- **Framework**: ScalaTest 3.2.19
- **Spark Session**: `local[2]`, `spark.ui.enabled=false`
- **Iceberg Catalog**: In-memory (`spark.sql.catalogImplementation=in-memory`) for speed
- **Structure**: Given-When-Then with clear assertions
- **Mocking**: Not required; use real SparkSession with in-memory catalog

### Integration Test Pattern
- **Spark Session**: `local[2]`
- **Iceberg Catalog**: Hadoop catalog on local filesystem (`target/iceberg-warehouse`)
- **Test Data**: Customer DataFrame from input.md example
- **Verification**:
  - Table exists at expected path
  - Data is partitioned by event_date (check `spark.read.format("iceberg").load(...).schema` or Iceberg API)
  - Append works correctly (count increases, no overwrite)
  - Schema matches input exactly

---

## 4. Convergence Criteria

| Criterion | Pass Condition |
|-----------|----------------|
| `mvn clean compile` | Compiles without errors |
| `mvn test` | All tests pass (unit + integration) |
| Main.scala delegates | No Iceberg write logic in Main.main; loads config via PureConfig |
| HOCON config used | Table identifier, warehouse, catalog from config, not hardcoded |
| Partitioning by event_date | Iceberg table has event_date as partition column (verify via Iceberg API or Spark) |
| writeTo API used | IcebergWriter uses `df.writeTo(...).using("iceberg").partitionedBy(...)` |
| Create/append semantics | First write creates table; subsequent writes append without overwrite |
| PureConfig loading | Config case classes loaded from `iceberg-config.conf` without code changes |

---

## 5. Milestones

| Milestone | Artifact | Status |
|-----------|----------|--------|
| SPEC approved | `specs/TEST-002/spec.md` | Done |
| PLAN created | `specs/TEST-002/plan.md` | Done |
| Config case classes updated | `IcebergConfig.scala` | Pending |
| HOCON config updated | `iceberg-config.conf` | Pending |
| Iceberg dependency added | `pom.xml` | Pending |
| IcebergWriter rewritten | `IcebergWriter.scala` | Pending |
| Main.scala updated | `Main.scala` | Pending |
| Unit tests pass | `mvn test` | Pending |
| Integration tests pass | `mvn test` | Pending |
| REVIEW completed | `specs/TEST-002/review.md` | Pending |

---

## 6. Implementation Order (Dependency-Aware)

1. **Task 1** → **Task 2** → **Task 3** (foundation: config + dependency)
2. **Task 4** (core logic, depends on 1-3)
3. **Task 5** (orchestration, depends on 1, 4)
4. **Task 6** → **Task 7** (tests, depend on 1-5)

---

**Plan updated from spec.md v2.0 for TEST-002.**