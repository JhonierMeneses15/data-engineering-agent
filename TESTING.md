# Testing Policy - data-engineering-agent

## Framework
- **ScalaTest 3.2.19** with `scalatest-maven-plugin 2.2.0`
- Structure: `src/test/scala/**/*Test.scala`
- Runner: Maven (`mvn test`)

## Strategy
- **test-after** by default (existing tests are post-implementation)
- **TDD optional** — planner decides per ticket based on complexity
- Property-based testing with **ScalaCheck** available if added as dependency

## Actual Commands
```bash
# Compile
mvn clean compile

# Tests (Java 17 + Spark 3.5.2 compatibility handled by .mvn/jvm.config)
mvn test
```

## Conventions
- Names: `should_<expected_behavior>` or `Given_When_Then`
- Unit tests: fast (<100ms), no SparkSession if possible
- Integration tests: with SparkSession local[2], `spark.ui.enabled=false`
- Fixtures: case classes in `src/test/scala/.../fixtures/`

## Spark Testing
- Use `local[*]` in main, `local[2]` in tests
- `spark.ui.enabled=false` to avoid UI port
- `spark.sql.adaptive.enabled=true` by default
- Stop SparkSession in try/finally ALWAYS

## Coverage
- `scoverage` not currently configured — not required
- Informal target: > 80% on domain logic

## CI Validation
- `mvn clean test` must pass
- No flaky tests
- Reasonable max time: 5 min

## TDD
- **Not mandatory** universally
- planner-agent decides per ticket based on:
  - New complex functionality → TDD recommended
  - Bug fix → regression test first
  - Critical refactoring → characterization tests
  - Documentation only → none