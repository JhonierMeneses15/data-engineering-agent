# TEST-002 — Write Customer Data to a Partitioned Iceberg Table

## Functional Requirements

### FR-001: Write Customer DataFrame to Iceberg Table
The system shall provide a function that accepts a Spark DataFrame with schema `(customer_id: String, name: String, age: Int, event_date: Date)` and writes it to an Apache Iceberg table.

### FR-002: Create Table If Not Exists
The system shall create the Iceberg table with the correct schema and partitioning by `event_date` if the table does not exist.

### FR-003: Append to Existing Table
The system shall append new records to the Iceberg table without overwriting existing data when the table already exists.

### FR-004: Partition by event_date
The Iceberg table shall be partitioned by the `event_date` field.

### FR-005: Configurable Table Identifier
The Iceberg table identifier (namespace.table) shall be configurable via HOCON configuration.

### FR-006: Configurable Warehouse/Catalog
The Iceberg warehouse path and catalog configuration shall be externalized via HOCON configuration, separate from transformation logic.

### FR-007: Separate Iceberg Writer Module
The Iceberg write logic shall reside in a separate Scala module/class from `Main.scala`, with `Main.scala` delegating to this module.

### FR-008: Preserve Input Schema
The Iceberg table schema shall match the input DataFrame schema exactly.

## Non-Functional Requirements

### NFR-001: Technology Stack
- Scala 2.12.19
- Spark 3.5.2
- Apache Iceberg (compatible with Spark 3.5.2)
- Maven for build
- PureConfig for HOCON configuration

### NFR-002: Configuration Management
All Iceberg-related configuration (table identifier, warehouse path, catalog properties) must be loaded from `application.conf` using PureConfig — no hardcoded values in code.

### NFR-003: Code Organization
- Iceberg writer logic in `src/main/scala/com/example/iceberg/IcebergWriter.scala`
- Configuration case classes in `src/main/scala/com/example/config/IcebergConfig.scala`
- `Main.scala` only orchestrates: reads config, creates SparkSession, reads input, calls writer

### NFR-004: Resource Management
SparkSession must use try/finally pattern with local implicits (`import spark.implicits._` inside try block).

## Acceptance Criteria

### AC-001: Write to Partitioned Iceberg Table
```gherkin
Given a Spark DataFrame with columns (customer_id, name, age, event_date)
When the Iceberg writer process executes
Then the data is written to an Iceberg table partitioned by event_date
```
**Linked FR:** FR-001, FR-004

### AC-002: Create Table with Correct Schema and Partitioning
```gherkin
Given the Iceberg table does not exist
When the Iceberg writer process executes
Then the table is created with schema matching the input DataFrame and partitioned by event_date
```
**Linked FR:** FR-002, FR-004, FR-008

### AC-003: Append to Existing Table
```gherkin
Given the Iceberg table already exists with data
When the Iceberg writer process executes with new records
Then new records are appended without overwriting existing data
```
**Linked FR:** FR-003

### AC-004: Configuration Change Without Code Changes
```gherkin
Given a modified application.conf with different table identifier or warehouse path
When the Iceberg writer process executes
Then it uses the new configuration values without any code changes
```
**Linked FR:** FR-005, FR-006

### AC-005: Main.scala Delegates to Separate Writer Module
```gherkin
Given Main.scala is inspected
When reviewing the code
Then it contains no Iceberg write logic and delegates to IcebergWriter module
```
**Linked FR:** FR-007

## Configuration Schema

### application.conf Structure
```hocon
iceberg {
  catalog {
    type = "hadoop"  # or "hive", "rest"
    warehouse = "file:///tmp/iceberg/warehouse"
    # Additional catalog properties
    "hive.metastore.uris" = "thrift://localhost:9083"
  }
  table {
    identifier = "default.customers"
  }
  write {
    format = "parquet"
    target-file-size-bytes = 134217728  # 128MB
  }
}
```

### Configuration Case Classes
```scala
case class IcebergCatalogConfig(
  `type`: String,
  warehouse: String,
  properties: Map[String, String] = Map.empty
)

case class IcebergTableConfig(
  identifier: String
)

case class IcebergWriteConfig(
  format: String = "parquet",
  `target-file-size-bytes`: Long = 134217728L
)

case class IcebergConfig(
  catalog: IcebergCatalogConfig,
  table: IcebergTableConfig,
  write: IcebergWriteConfig
)
```

## Interface Specification

### IcebergWriter Trait/Class
```scala
package com.example.iceberg

import org.apache.spark.sql.{DataFrame, SparkSession}

trait IcebergWriter {
  def write(df: DataFrame, config: IcebergConfig)(implicit spark: SparkSession): Unit
}

object IcebergWriter {
  def apply(): IcebergWriter = new IcebergWriterImpl()
}
```

### Implementation Requirements
- Use `df.writeTo(config.table.identifier).using("iceberg").tableProperty(...)` API
- Set partition field via `.partitionedBy($"event_date")` or equivalent
- Use `.createOrReplace()` for create-if-not-exists + append semantics, or `.append()` for existing table
- Configure catalog via SparkSession options or `spark.sql.catalog.<name>` properties

## Test Specification

### Unit Tests
- Test IcebergWriter logic with mocked SparkSession/DataFrame
- Verify correct Iceberg API calls (writeTo, partitionedBy, createOrReplace/append)
- Test configuration parsing from HOCON

### Integration Tests
- Use local Iceberg catalog (Hadoop catalog on local filesystem)
- Write sample DataFrame, verify table exists with correct partitioning
- Write second batch, verify append behavior (count increases, no overwrite)
- Verify schema preservation

## Out of Scope
- Reading from Iceberg tables
- Time travel / snapshot management
- Schema evolution beyond basic append compatibility
- Streaming writes (batch only)
- Custom Iceberg catalog implementations beyond standard types

## Dependencies to Add (if not present)
```xml
<!-- Iceberg Spark Runtime -->
<dependency>
  <groupId>org.apache.iceberg</groupId>
  <artifactId>iceberg-spark-runtime-3.5_2.12</artifactId>
  <version>1.6.1</version>
</dependency>

<!-- PureConfig for HOCON -->
<dependency>
  <groupId>com.github.pureconfig</groupId>
  <artifactId>pureconfig_2.12</artifactId>
  <version>0.17.6</version>
</dependency>
```

## Open Questions Resolution
1. **Catalog type**: Default to Hadoop catalog (`type = "hadoop"`) for local testing simplicity
2. **PureConfig dependency**: Yes, add as specified above