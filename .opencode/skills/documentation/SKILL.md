---
name: documentation
description: Knowledge for updating project documentation
---

# Documentation Skill

## Targets by Priority
1. README.md (immediate visibility)
2. CHANGELOG.md (history)
3. docs/adr/ (architectural decisions)
4. Scaladoc (public API)
5. docs/guides/ (how-to)
6. Confluence (if tool available)

## When to Document
- spec.md → documentation_impact = required → ALWAYS
- documentation_impact = auto → IF user-visible change
- documentation_impact = none → NEVER

## Style
- Markdown, tables for options
- Compilable, tested code examples
- Links to related tickets/PRs