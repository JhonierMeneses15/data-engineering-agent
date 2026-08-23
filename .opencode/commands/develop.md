---
description: Run the complete spec-driven development workflow for a ticket
agent: development-agent
subtask: true
---

# /develop <ticket-id>

## Context
- Ticket ID: {{args}}
- Input file: `specs/{{args}}/input.md`
- Output directory: `specs/{{args}}/`

## Instructions
You are the development-agent. Execute the complete spec-driven development workflow for the given ticket.

## Workflow
1. Read the ticket input from `specs/{{args}}/input.md`
2. Read project policies: AGENTS.md, CONSTITUTION.md, TESTING.md
3. Execute the complete workflow:
   - SPEC: Invoke spec-agent → wait for `specs/{{args}}/spec.md`
   - PLAN: Invoke planner-agent → wait for `specs/{{args}}/plan.md`
   - IMPLEMENT: Invoke implementer-agent → wait for IMPLEMENTATION_COMPLETE
   - REVIEW: Invoke reviewer-agent → wait for `specs/{{args}}/review.md`

## Review Loop (Max 3 Cycles)
- If reviewer returns APPROVE → workflow complete, report success
- If reviewer returns REQUEST_CHANGES → return to IMPLEMENT with findings (max 3 cycles)
- If reviewer returns BLOCK with requirement issue → report BLOCKED, stop
- If reviewer returns BLOCK with design/implementation issue → return to IMPLEMENT

## Error Handling
- If any agent fails or times out → report BLOCKED
- If model unavailable → try configured fallback, record fallback used, else BLOCKED
- If artifact missing after agent completion → report BLOCKED
- If requirements ambiguous → document in spec.md, do not invent decisions

## Output
On success, report:
- Ticket ID
- What changed (files modified)
- Tests added/executed
- Review decision (APPROVE)
- Artifact paths

On failure/blocked:
- Ticket ID
- Stage where blocked
- Exact reason
- Required human decision

## Artifact Locations
All artifacts in `specs/{{args}}/`:

- `input.md` (provided)
- `spec.md` (spec-agent output)
- `plan.md` (planner-agent output)
- `review.md` (reviewer-agent output)

Do NOT create any other artifacts.
Do NOT execute git commands.
Do NOT modify files outside the workflow.