---
name: testing
description: Knowledge for testing adapted to each repository
---

# Testing Skill

## Golden Rule
**CONSULT TESTING.md OF THE REPO FIRST**

## Strategy by Repository
1. Find TESTING.md in root
2. If exists → use that policy
3. If not → infer from existing tests + build tool
4. Fallback: minimal conventional configuration
5. Record: testing_policy.source = explicit|inferred|default

## This Repo (Scala + Maven + ScalaTest)
- Framework: ScalaTest 3.2.x
- Structure: src/test/scala/**/*Test.scala
- Command: `mvn test`
- Spark testing: local[*] in tests, `spark.ui.enabled=false`
- Property-based: ScalaCheck optional
- Coverage: scoverage not configured

## TDD
- Only if TESTING.md specifies
- test-design-agent defines order
- implementer-agent follows TDD order

## Anti-patterns
- Tests only verifying "no exception thrown"
- Excessive mocks of Spark internals
- Slow integration tests without testcontainers