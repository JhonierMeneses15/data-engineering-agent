# AGENTS.md - Global Repository Rules

## Tech Stack
- **Scala**: 2.12.19
- **Spark**: 3.5.2
- **Java**: 17
- **Build**: Maven 3.9+ (`scala-maven-plugin` 4.9.2)
- **Test**: ScalaTest 3.2.19 (`scalatest-maven-plugin` 2.2.0)
- **Exec**: `exec-maven-plugin` 3.5.1

## Essential Commands
```bash
# Compile
mvn clean compile

# Tests
mvn test

# Run Main
mvn exec:java
```

## Spec-Driven Development Workflow
Use the `/develop` command for ticket-based development:

```bash
/develop <ticket-id>
```

This invokes the `development-agent` which orchestrates:
1. **SPEC** - spec-agent creates `specs/<ticket-id>/spec.md`
2. **PLAN** - planner-agent creates `specs/<ticket-id>/plan.md`
3. **IMPLEMENT** - implementer-agent implements code + tests
4. **REVIEW** - reviewer-agent creates `specs/<ticket-id>/review.md`

Agents communicate through artifacts in `specs/<ticket-id>/`:
- `input.md` (user-provided)
- `spec.md` (spec-agent output)
- `plan.md` (planner-agent output)
- `review.md` (reviewer-agent output)

## Code Conventions
1. **Case classes** for DTOs/domain models (type-safe)
2. **try/finally** for `SparkSession` — ALWAYS
3. **Local implicits** (`import spark.implicits._` inside try)
4. **Pure functions** separated from I/O/Spark
5. **Config via HOCON** (PureConfig) — NO hardcoding
6. **No logic in `Main.main`** — delegate to testable functions
7. **Structured logging** (log4j2 JSON) — no `println` in production

## Critical Prohibitions
- ❌ Modify `pom.xml` unless strictly necessary
- ❌ Migrate to SBT
- ❌ Add `scalafmt`/`scalafix` without configuring and validating
- ❌ Hardcode `master`/`appName` in code
- ❌ Introduce new dependencies without justification
- ❌ Use OpenRouter or paid models
- ❌ Introduce Docker/K8s/Kafka/JDBC unnecessarily

## Repository Policies to Consult
- `TESTING.md` — testing strategy
- `CONSTITUTION.md` — global principles
- `.opencode/skills/scala-spark/SKILL.md` — Scala/Spark rules
- `.opencode/skills/spec-writing/SKILL.md` — spec writing
- `.opencode/skills/testing/SKILL.md` — testing

## Agent Behavior
- **Least privilege**: each agent only necessary permissions
- **Spec-first**: no implementation without approved spec
- **Validator decides done**: implementer never declares "done"
- **Independent reviewer**: different model and agent from implementer
- **Mandatory convergence**: max 3 review/implementation cycles
- **Documentation**: only when `documentation_impact != none`
- **Artifact-based communication**: agents read/write files, no conversational summaries