# Specs

Feature artifacts live here. Each feature has its own folder named after its slug.

```text
specs/<slug>/
├── spec.md       ← requirements and acceptance scenarios (source of truth)
├── plan.md       ← implementation design and approach
├── tasks.md      ← execution checklist (checked off as work lands)
└── research.md   ← optional: investigation notes, benchmarks, prior art
```

## Slug Convention

The slug is derived from whatever identifier is available:

| Situation | Slug format | Example folder |
|---|---|---|
| GitHub issue exists | `ISSUE-short-name` | `specs/730-sentinel-connection/` |
| Jira ticket exists | `RED-1234-short-name` | `specs/RED-1234-sentinel-connection/` |
| Neither | `short-name` | `specs/sentinel-connection/` |

The branch name is `<type>/<slug>` (e.g. `feat/`, `fix/`, `docs/`, `chore/`), so
the spec folder can always be found by stripping everything up to and including
the first `/` from the branch name.

## Starting a New Spec

1. Run `/speckit-specify <description>` — it picks the slug, creates the branch and folder.
2. Get `spec.md` approved before running `/speckit-plan`.
3. Get `plan.md` approved before running `/speckit-tasks`.
4. Get `tasks.md` approved before running `/speckit-implement`.
5. Update `AGENTS.md` → **Recent Changes** when the feature merges.

## Spec Lifecycle

| Status | Meaning |
|---|---|
| Draft | Being written, not yet reviewed |
| In Review | Open for feedback |
| Approved | Implementation may begin |
| Implemented | Code merged to `main` |
| Rejected | Not proceeding — kept for record |
