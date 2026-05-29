---
name: speckit-specify
description: Create a new feature spec. Use when starting a new feature to generate
  specs/<slug>/spec.md with requirements, acceptance scenarios, and scope
  boundaries. Follows the spec-driven workflow required by this repo.
---

# Speckit Specify

## User Input

```text
$ARGUMENTS
```

Given the feature description above, do the following:

## Steps

### 1. Choose a slug

The slug becomes both the branch suffix and the spec folder name. Derive it from
whatever identifier is available:

| Situation | Slug format | Example |
|---|---|---|
| GitHub issue exists | `ISSUE-short-name` | `730-sentinel-connection` |
| Jira ticket exists | `RED-1234-short-name` | `RED-1234-sentinel-connection` |
| Neither | `short-name` | `sentinel-connection` |

- Use lowercase kebab-case, 2–4 words for the short-name part
- Show the proposed slug and ask the user to confirm before creating anything

### 2. Choose a branch name

```
<type>/<slug>
```

- `<type>` is a short prefix describing the nature of the change: `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, or similar
- **Must be under 50 characters total**
- Show the proposed branch name alongside the slug for confirmation

### 3. Create the spec folder and branch

Use the type and slug confirmed in step 2:

```bash
mkdir -p specs/<slug>
git checkout -b <type>/<slug>
```

### 4. Write `specs/<slug>/spec.md`

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
- Spec path: `specs/<slug>/spec.md`
- Branch: `<type>/<slug>` (whichever was created)
- Any open questions remaining
- Next step: run `/speckit-plan`
