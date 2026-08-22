---
name: task-decomposition
description: Knowledge for decomposing plans into atomic executable tasks
---

# Task Decomposition Skill

## Principles
- Atomic tasks (single responsibility)
- done_when verifiable automatically
- Explicit dependencies (DAG)
- parallelizable = true only if no shared state

## Target Granularity
- 30 min - 2 hours per task
- If > 4 hours → split
- If < 15 min → combine

## Template
```yaml
- id: TASK-001
  description: "Create RiskCategory case class with validation"
  dependencies: []
  files: ["src/main/scala/.../RiskCategory.scala"]
  tests: ["TEST-001"]
  done_when: "Compiles + unit test passes"
  parallelizable: true
  estimated_effort: S
```