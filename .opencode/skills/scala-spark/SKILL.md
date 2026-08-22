---
name: scala-spark
description: Repository-specific technical rules for data-engineering-agent
---

# Scala-Spark Skill (Repository-Specific)

## CURRENT REPOSITORY FACTS

### Fixed Stack
- Scala: 2.12.19
- Spark: 3.5.2
- Java: 17
- Build: Maven (scala-maven-plugin 4.9.2)
- Test: ScalaTest 3.2.19 + scalatest-maven-plugin 2.2.0

### Actual Spark Local Config
```scala
SparkSession.builder()
  .appName("...")
  .master("local[*]")
  .config("spark.ui.enabled", "false")
  .config("spark.sql.adaptive.enabled", "true")
  .getOrCreate()
```

### Java 17 + Spark 3.5.2 Workaround
File `.mvn/jvm.config` already contains:
```
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED
```
Use normal Maven command: `mvn test`

### Existing Code Conventions
- Avoid `.collect()` in production
- Prefer DataFrame API over RDD

## RECOMMENDED PATTERNS (not enforced, just guidance)

### Config
- PureConfig + HOCON for external config (no hardcoding)

### Logging
- log4j2 JSON structured logging (no println in production)

### Architecture
- Separate pure logic from I/O/Spark
- Testable functions without Spark dependencies
- Delegate from Main.main to pure functions

## CONFIRMED PROHIBITIONS
- ❌ Hardcode master/appName in main
- ❌ println in production code
- ❌ Business logic in Main.main
- ❌ Modify pom.xml without strict need
- ❌ Migrate to SBT
- ❌ Add scalafmt/scalafix without configuring and validating
- ❌ Introduce new dependencies without justification
- ❌ Use OpenRouter or paid models
- ❌ Introduce Docker/K8s/Kafka/JDBC unnecessarily

## NOTE
Do NOT claim PureConfig, HOCON, log4j2 JSON, scoverage, SparkTestingBase exist if not configured. Only document what EXISTS.