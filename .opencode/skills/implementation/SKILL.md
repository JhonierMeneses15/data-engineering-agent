---
name: implementation
description: Implementation patterns and TDD workflow
---

# Implementation Skill

## Patterns Scala/Spark (this repo)
- Case classes for DTOs/domain models
- Try/finally for SparkSession
- Local implicits, not globals
- Pure functions separated from I/O
- Config via HOCON (PureConfig) no hardcoded

## TDD Workflow (if applicable)
1. Write failing test (RED)
2. Implement minimum to pass (GREEN)
3. Refactor (REFACTOR)
4. Logical commit

## Error Handling
- Either/Result types for expected errors
- Exceptions only for bugs/unexpected
- Structured logging (log4j2 JSON)
- Retry with backoff for external I/O

## Commits
- Logical, not giant nor excessively atomic
- Convention: <type>(<scope>): <description>
- feat/fix/test/docs/refactor/chore