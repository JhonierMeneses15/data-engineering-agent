# Implementation Plan: Spec-Driven Agentic Development Harness

## Current State
- Repository: data-engineering-agent (Scala 2.12.19, Spark 3.5.2, Java 17, Maven)
- Existing OpenCode config: opencode.json with warp plugin
- No TESTING.md, CONSTITUTION.md, updated AGENTS.md
- No .opencode/agents, .opencode/skills, .opencode/commands structure
- No .harness/ structure

## Implementation Phases

### Phase 1: Base Repository Documents
- [ ] TESTING.md (actual repo testing policy)
- [ ] CONSTITUTION.md (global principles)
- [ ] AGENTS.md (global rules + references)

### Phase 2: OpenCode Native Configuration (.opencode/)
- [ ] .opencode/agents/ (10 agents with native syntax)
- [ ] .opencode/skills/ (7 skills with SKILL.md + frontmatter)
- [ ] .opencode/commands/ (8 reusable LLM commands)

### Phase 3: Harness Configuration (.harness/)
- [ ] .harness/workflow.yaml (definitive 18-stage workflow)
- [ ] .harness/models.yaml (4 model profiles with verified FREE models)
- [ ] .harness/state-schema.yaml (states and transitions)
- [ ] .harness/failure-routing.yaml (explicit routing)
- [ ] .harness/artifact-schemas/ (18 artifact contracts)
- [ ] .harness/policies/git.yaml
- [ ] .harness/policies/documentation.yaml
- [ ] .harness/constitution.yaml

### Phase 4: Validation
- [ ] Verify OpenCode native syntax
- [ ] Verify available FREE models
- [ ] Check OpenCode vs Harness separation
- [ ] Generate final summary

## Verified FREE OpenCode Zen Models (August 2026)

| Profile | Primary Model | Fallback | Context |
|---------|---------------|----------|---------|
| fast-free | nemotron-3.5-lightning-free | - | 1M |
| reasoning-free | hy3-free | nemotron-3.5-lightning-free | 256K |
| coding-free | muse-spark-1.2-free | nemotron-3.5-lightning-free, big-pickle | 1M |
| spec-free | muse-spark-1.2-free | nemotron-3.5-lightning-free | 1M |

Verification date: 2026-08-22

## Agents (11) - Model Profile Mapping

| Agent | Model Profile | Permissions |
|-------|---------------|-------------|
| clarify-agent | fast-free | read-only |
| spec-agent | spec-free | read-only |
| planner-agent | reasoning-free | read-only |
| test-design-agent | spec-free | read-only |
| task-agent | fast-free | read-only |
| analyze-agent | reasoning-free | read-only |
| implementer-agent | coding-free | read, edit, bash (granular) |
| convergence-agent | spec-free | read-only |
| reviewer-agent | reasoning-free | read-only |
| documentation-agent | fast-free | docs-only edit |

## Skills (7 + 1 new)

1. spec-writing
2. software-planning
3. testing
4. implementation
5. code-review
6. documentation
7. scala-spark
8. task-decomposition (new)

## Commands (9) - LLM stages only

1. /spec
2. /plan
3. /test-design
4. /tasks
5. /analyze
6. /implement
7. /converge
8. /review
9. /document

## Workflow Stages (18)

1. PREPARE_WORKSPACE (deterministic)
2. CLARIFY (LLM)
3. SPEC (LLM)
4. HUMAN_APPROVAL (Human gate)
5. PLAN (LLM)
6. TEST_DESIGN (LLM)
7. TASKS (LLM)
8. ANALYZE (LLM quality gate)
9. IMPLEMENT (LLM)
10. VERIFY (Deterministic: mvn clean compile + mvn test)
11. CONVERGE (LLM, max 3 iterations)
12. REVIEW (LLM independent)
12. DOCUMENTATION (LLM, optional)
14. PRE_COMMIT_CHECK (Deterministic)
15. COMMIT (Deterministic, logical)
16. READY_FOR_PR (Human gate optional)
17. PUSH (Deterministic)
18. CREATE_PR (Deterministic, fallback manual)
19. DONE (Terminal)

## Critical Separation

| OpenCode (.opencode/) | Harness (.harness/) |
|----------------------|---------------------|
| agents (native syntax) | workflow.yaml |
| skills (SKILL.md) | models.yaml |
| commands (native syntax) | state-schema.yaml |
| | failure-routing.yaml |
| | artifact-schemas/ |
| | policies/ |
| | constitution.yaml |

DO NOT mix: model_profile, workflow states, artifact schemas, failure routing → .harness/