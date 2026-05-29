---
name: speckit-specify
description: Create a new feature spec. Use when starting a new feature to generate
  specs/NNN-feature-name/spec.md with requirements, acceptance scenarios, and scope
  boundaries. Follows the spec-driven workflow required by this repo.
---

# Speckit Specify

## User Input

```text
$ARGUMENTS
```

Given the feature description above, do the following:

## Steps

### 1. Determine the spec folder number

List `specs/` and find the highest `NNN-` prefix. Use the next number (zero-padded to three digits).

### 2. Choose a branch name

- Format: `feat/NNN-short-description` or `fix/NNN-short-description`
- **Must be under 40 characters total**
- Use lowercase kebab-case for the description (2–4 words)
- If a GitHub issue number is given, prefer `feat/ISSUE-short-name`
- Show the proposed branch name and ask the user to confirm before creating it

### 3. Create the spec folder and branch

```bash
mkdir -p specs/NNN-feature-name
git checkout -b feat/NNN-feature-name
```

### 4. Write `specs/NNN-feature-name/spec.md`

Use the structure from `specs/SPEC_TEMPLATE/spec.md`. Fill in:

- **Problem** — one paragraph from the Spring developer's perspective
- **Goals** — testable bullet list (each goal is done or not)
- **Non-Goals** — explicit scope boundaries
- **Background** — links to GitHub issues, Redis docs, prior art
- **Proposed API** — minimal Java code example of the public surface; list new annotations, repository methods, config properties
- **Acceptance Scenarios** — at least: happy path, one edge case, one error case
- **Compatibility & Migration** — breaking changes or deprecations
- **Open Questions** — mark with `- [ ]`

Make informed guesses for unspecified details. Use `[NEEDS CLARIFICATION: question]` only for decisions that materially affect scope or public API — maximum 3 markers.

### 5. Validate and resolve clarifications

After writing the spec, check:

- [ ] No implementation details (no class names, framework internals, SQL)
- [ ] Every Goal is testable
- [ ] Acceptance Scenarios cover happy path + edge + error
- [ ] Scope is explicitly bounded in Non-Goals

If `[NEEDS CLARIFICATION]` markers remain, present them as numbered questions with 2–3 options each. Wait for answers, then update the spec.

### 6. Report completion

Output:
- Spec path: `specs/NNN-feature-name/spec.md`
- Branch: `feat/NNN-feature-name`
- Any open questions remaining
- Next step: run `/speckit-plan`
