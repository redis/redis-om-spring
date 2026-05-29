---
name: speckit-tasks
description: Break an approved implementation plan into an ordered task checklist.
  Use after plan.md is approved to create specs/<slug>/tasks.md.
  Each task must be specific enough to implement without additional context.
---

# Speckit Tasks

## User Input

```text
$ARGUMENTS
```

## Prerequisites

- `spec.md` — Status: Approved
- `plan.md` — Status: Approved

Stop and say which artifact is missing or not yet approved if either condition fails.

## Steps

### 1. Load context

Read both `spec.md` and `plan.md` from the active spec folder.

### 2. Write `specs/<slug>/tasks.md`

Use the structure from `specs/SPEC_TEMPLATE/tasks.md`. Organize tasks into phases:

**Phase 1 — Implementation** (in `redis-om-spring/` or `redis-om-spring-ai/`)
- One task per meaningful unit of work
- Include the exact file path for each task
- Mark parallelizable tasks `[P]`

**Phase 2 — Tests** (all in `tests/src/test/java/com/redis/om/spring/`)
- Happy-path test
- Edge-case test(s)
- Error/negative test
- Tests must map 1:1 to Acceptance Scenarios in spec.md

**Phase 3 — Docs**
- Only if user-visible behavior changes
- Exact page path under `docs/content/modules/ROOT/pages/`

**Phase 4 — Cleanup**
- `./gradlew spotlessApply build aggregateTestReport -S` — all green
- Update `AGENTS.md` → Recent Changes
- Mark spec status `Implemented`

### Task format

Every task must follow this exact format:

```
- [ ] T001 Description with exact/file/path.java
- [ ] T002 [P] Description that can run in parallel with exact/file/path.java
```

Rules:
- Sequential ID: T001, T002, T003 …
- `[P]` only when the task touches different files from concurrent tasks
- Description must be actionable without reading the plan again
- Include the file path for every code or doc change

### 3. Validate

- [ ] Every Acceptance Scenario from spec.md has at least one test task
- [ ] No test tasks target `redis-om-spring/` or `redis-om-spring-ai/` — only `tests/`
- [ ] All tasks have file paths
- [ ] Cleanup phase is present

### 4. Report completion

Output:
- Tasks path: `specs/<slug>/tasks.md`
- Total task count and breakdown by phase
- Next step: get tasks approved, then run `/speckit-implement`
