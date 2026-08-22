---
name: code-review
description: Code review checklist for quality gates
---

# Code Review Skill

## Required Checklist
### Security
- [ ] No secrets/keys in code
- [ ] Input validation/sanitization
- [ ] No SQL injection (params bind)
- [ ] No path traversal

### Performance (Spark)
- [ ] Avoid collect() on large datasets
- [ ] Broadcast joins when appropriate
- [ ] Proper partitioning
- [ ] Cache/uncache correct
- [ ] Serialization (Kryo)

### Maintainability
- [ ] Clear names (intention-revealing)
- [ ] Functions < 50 lines
- [ ] No duplicate code (DRY)
- [ ] Explicit types in public APIs
- [ ] Scaladoc in public APIs

### Architecture
- [ ] Layer separation (domain/infra)
- [ ] Dependency injection
- [ ] No coupling to concrete implementations

### Tests
- [ ] Edge case coverage
- [ ] Descriptive names (Given_When_Then)
- [ ] Deterministic (no flakiness)
- [ ] Fast (unit < 100ms)