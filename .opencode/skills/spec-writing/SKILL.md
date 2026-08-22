---
name: spec-writing
description: Reusable knowledge for writing verifiable specifications
---

# Spec Writing Skill

## Purpose
Reusable knowledge for writing verifiable specifications that serve as contracts between stages.

## Spec Template
See spec-agent.md structure

## Acceptance Criteria (Gherkin)
```gherkin
Given <initial context>
When <action>
Then <observable result>
```

## Traceability
- Each FR → ≥1 AC
- Each AC → unique ID (AC-XXX)
- ACs are basis for tests.md

## Anti-patterns
- Vague requirements ("fast", "easy to use")
- Non-verifiable ACs ("works well")
- Mixing WHAT with HOW
- ACs without linked_fr