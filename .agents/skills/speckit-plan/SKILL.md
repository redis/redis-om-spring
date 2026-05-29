---
name: speckit-plan
description: Generate a technical implementation plan from an approved feature spec.
  Use after spec.md is approved to create specs/<slug>/plan.md with affected modules,
  key classes, Redis commands, and ordered implementation steps.
---

# Speckit Plan

## User Input

```text
$ARGUMENTS
```

## Prerequisites

- `spec.md` must exist and have **Status: Approved** before running this skill.
- If spec.md is still Draft or In Review, stop and say so.

## Steps

### 1. Locate the active spec

Derive the spec folder by stripping everything up to and including the first `/` from the branch name:

```bash
git rev-parse --abbrev-ref HEAD   # e.g. feat/730-sentinel-connection → specs/730-sentinel-connection/
```

Read `specs/<slug>/spec.md` in full.

### 2. Research if needed

For any unknown Redis commands, Spring Data patterns, or integration points:
- Check existing usages in `redis-om-spring/src/main/java/com/redis/om/spring/`
- Check existing tests in `tests/src/test/java/com/redis/om/spring/` for patterns
- Note decisions and alternatives in the plan

### 3. Write `specs/<slug>/plan.md`

Use the structure from `specs/SPEC_TEMPLATE/plan.md`. Fill in:

**Approach** — one paragraph: chosen direction and the key trade-off that decided it.

**Affected Modules** — table:

| Module | Change |
|---|---|
| `redis-om-spring/` | what changes |
| `redis-om-spring-ai/` | what changes or "none" |
| `tests/` | new test classes |
| `docs/` | pages to update or "none" |

**Key Classes** — list new and modified classes with full package paths.

**Redis Commands / Data Structures** — which Redis commands this feature relies on.

**Implementation Steps** — ordered list. Each step maps to one or more tasks.md entries. Steps must be sequenced so each is independently buildable.

**Alternatives Rejected** — brief note on what else was considered and why rejected.

**Open Questions** — unresolved decisions needed before or during implementation.

### 4. Validate

- [ ] No step requires modifying test infrastructure in the library modules (tests go in `tests/`)
- [ ] No new Maven/mvnw references
- [ ] Java version assumed is 21
- [ ] All new dependencies are justified

### 5. Report completion

Output:
- Plan path: `specs/<slug>/plan.md`
- Summary of affected modules
- Any open questions blocking implementation
- Next step: run `/speckit-tasks`
