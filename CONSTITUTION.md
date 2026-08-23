# Constitution - Global Harness Principles

1. **Spec is source of truth.** The approved specification is the immutable development contract.

2. **No implementation before approved spec.** No production code written without human-approved spec.

3. **No silent requirement changes.** Changes to intent require returning to SPEC and regenerating plan/tests/tasks.

4. **Minimal change.** Implement only what spec requires. No gold-plating.

5. **Functional changes require validation.** Every functional change must pass deterministic verification (build + tests).

6. **Repository policies override generic assumptions.** TESTING.md, AGENTS.md, repo skills take priority over defaults.

7. **Deterministic verification before semantic convergence.** VERIFY (mvn test) passes before CONVERGE (LLM semantic review).

8. **Human approval before external side effects.** Mandatory gates: post-SPEC and pre-PUSH/PR.

9. **Agents have least privilege.** Each agent only necessary permissions. Implementer is the only one with edit/bash.

10. **Evidence is required for completion.** result.json must contain metrics, artifacts, and full traceability.

11. **Failures are never silently ignored.** Every error has explicit routing and responsible owner.