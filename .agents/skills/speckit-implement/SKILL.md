---
name: speckit-implement
description: Execute all tasks from tasks.md to implement a feature. Use after
  spec, plan, and tasks are all approved. Works through the checklist phase by phase,
  marks tasks complete, and verifies the build before handoff.
---

# Speckit Implement

## User Input

```text
$ARGUMENTS
```

## Prerequisites

All three must exist and be approved:

- `specs/NNN-feature-name/spec.md` — Status: Approved
- `specs/NNN-feature-name/plan.md` — Status: Approved
- `specs/NNN-feature-name/tasks.md` — Status: Approved

Stop and say which artifact is missing or not yet approved.

## Steps

### 1. Load context

Read `spec.md`, `plan.md`, and `tasks.md` from the active spec folder in full.

### 2. Start Redis

```bash
docker compose up -d
```

Confirm Redis is reachable before proceeding.

### 3. Execute tasks phase by phase

For each phase in `tasks.md`:

- Run sequential tasks in order.
- Run `[P]` tasks concurrently where they touch different files.
- After each task, mark it complete in `tasks.md`: `- [ ]` → `- [x]`
- If a task fails, stop, report the failure, and wait for guidance before continuing.

### 4. Verify after each phase

After completing Phase 1 (implementation):

```bash
./gradlew spotlessApply compileJava -S
```

After completing Phase 2 (tests):

```bash
./gradlew spotlessApply build aggregateTestReport -S
```

All checks must be green before moving to the next phase. Fix failures immediately — do not skip or weaken tests.

### 5. Final verification

```bash
./gradlew spotlessCheck build aggregateTestReport -S
```

Report pass or fail. If failing, fix before marking the feature done.

### 6. Cleanup

- [ ] Update `AGENTS.md` → **Recent Changes**: append `- NNN-feature-name: one-line summary`
- [ ] Set `spec.md` status to `Implemented`

### 7. Report completion

Output:
- All tasks completed: yes/no (list any skipped with reason)
- Build status: pass/fail
- Files changed: summary list
- Branch ready for PR: yes/no
