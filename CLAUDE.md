# Claude Code Guide

Claude Code-specific overlay. The canonical agent guide for this repo is
[AGENTS.md](AGENTS.md) — read it first for workflow rules, the source-of-truth
reading order, and verification steps. Notes here only add Claude Code-specific
conventions; they do not duplicate or override `AGENTS.md`.

## Working in this repo

- `AGENTS.md` is the canonical source of truth — product overview, module map,
  working rules, build commands, CI, and known inconsistencies. Pull referenced
  docs **on demand**, not all at session start.
- The standard quality gate is `./gradlew spotlessCheck build aggregateTestReport -S`.
  Run `./gradlew spotlessApply` to fix formatting before every commit.
- Spec-driven feature work lives under [specs/](specs/) using a folder-per-feature
  layout (`spec.md`, `plan.md`, `tasks.md`). See [specs/README.md](specs/README.md).

## Slash commands

Project-shared commands live in [.claude/commands/](.claude/commands/):

- `/speckit-specify <description>` — create branch + `spec.md` for a new feature
- `/speckit-plan` — generate `plan.md` from an approved spec
- `/speckit-tasks` — generate `tasks.md` from an approved plan
- `/speckit-implement` — execute `tasks.md` phase by phase and verify the build

## Codex coexistence

Codex reads `AGENTS.md` as its primary guide; keep workflow content there.
This file and `.claude/` are Claude Code-only and do not affect Codex flows.
