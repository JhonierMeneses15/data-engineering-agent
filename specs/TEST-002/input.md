# TEST-002 — Write Customer Data to a Partitioned Iceberg Table

## Goal

Create a reusable Spark process that writes customer data into an Apache Iceberg table partitioned by event date.

## Input

The process receives a DataFrame with:

- `customer_id: String`
- `name: String`
- `age: Int`
- `event_date: Date`

Example:

| customer_id | name    | age | event_date  |
|-------------|---------|-----|-------------|
| C001        | Jhonier | 25  | 2026-08-22  |
| C002        | Alice   | 30  | 2026-08-22  |
| C003        | Bob     | 35  | 2026-08-23  |

## Requirement

Implement a reusable Spark process that:

1. Receives the customer DataFrame.
2. Writes the data to an Apache Iceberg table.
3. Creates the table if it does not exist.
4. Appends new records if the table already exists.
5. Uses `event_date` as the table partition field.
6. Preserves the input schema.
7. Keeps the Iceberg table identifier configurable.
8. Keeps the Iceberg warehouse/catalog configuration outside the transformation logic.
9. Keeps the Iceberg write logic separate from `Main.scala`.

## Partitioning

The Iceberg table must be partitioned by:

```text
event_date
```

## Acceptance Criteria

- AC-001: Given a customer DataFrame, when the process runs, then data is written to an Iceberg table partitioned by `event_date`.
- AC-002: Given the table does not exist, when the process runs, then the table is created with the correct schema and partitioning.
- AC-003: Given the table already exists, when the process runs, then new records are appended without overwriting existing data.
- AC-004: Given a configuration change (table name, warehouse path), when the process runs, then it uses the new configuration without code changes.
- AC-005: Given the Iceberg write logic is in a separate module, when Main.scala is inspected, then it delegates to the Iceberg writer without containing write logic.

## Constraints

- Must use Apache Iceberg with Spark 3.5.2
- Must use Scala 2.12.19
- Configuration via HOCON (PureConfig) — no hardcoding
- Iceberg write logic in a separate module from Main.scala
- Table identifier and warehouse path configurable via HOCON

## Out of Scope

- Reading from Iceberg (only write)
- Time travel / snapshot management
- Schema evolution handling beyond basic append
- Streaming writes (batch only)

## Testing Strategy

- Unit tests for the Iceberg writer logic (mock Spark/Iceberg if possible)
- Integration test with local Iceberg catalog (in-memory or local filesystem)
- Follow TESTING.md: ScalaTest, Maven, local[2] for tests

## Open Questions

- Which Iceberg catalog type to use by default (Hadoop, Hive, REST)?
- Should we add PureConfig dependency for HOCON config?