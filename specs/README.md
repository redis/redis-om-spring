# Specs

Feature artifacts live here. Each feature has its own numbered folder.

```text
specs/001-feature-name/
├── spec.md       ← requirements and acceptance scenarios (source of truth)
├── plan.md       ← implementation design and approach
├── tasks.md      ← execution checklist (checked off as work lands)
└── research.md   ← optional: investigation notes, benchmarks, prior art
```

## Naming Convention

Use a three-digit prefix to keep features ordered: `001-`, `002-`, etc.
The slug should be a short kebab-case description of the feature.

## Starting a New Spec

1. Create `specs/NNN-feature-name/`.
2. Copy `specs/SPEC_TEMPLATE/` files into it and fill them in.
3. Get spec approved before writing implementation code.
4. Update `AGENTS.md` → **Recent Changes** when the feature merges.

## Spec Lifecycle

| Status | Meaning |
|---|---|
| Draft | Being written, not yet reviewed |
| In Review | Open for feedback |
| Approved | Implementation may begin |
| Implemented | Code merged to `main` |
| Rejected | Not proceeding — kept for record |
