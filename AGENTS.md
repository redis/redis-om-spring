# Redis OM Spring Agent Guide

**Last updated**: 2026-05-29

## Project Overview

Redis OM Spring is a Gradle multi-module Java library that extends Spring Data Redis
with higher-level Redis-native modeling and query features. It is published to Maven
Central and consumed as a dependency by Spring Boot applications — not a deployable
service.

Agents work from the approved spec for the active feature. If a feature has no
approved spec yet, work from this file and existing conventions only.

## What the Library Does

Major capabilities:

- `@Document` for Redis JSON persistence
- Enhanced `@RedisHash` with Redis Query Engine integration
- `RedisDocumentRepository` and `RedisEnhancedRepository`
- Declarative indexing: `@Indexed`, `@Searchable`, `@GeoIndexed`, `@VectorIndexed`
- `EntityStream` — fluent, type-safe querying and aggregations
- Query derivation, `@Query`, and `@Aggregation`
- ULID generation for string IDs
- Multi-tenant dynamic index naming via SpEL
- Index migration: blue-green, dual-write, and in-place strategies
- Probabilistic data structures: Bloom, Cuckoo, Count-Min
- AI-powered vectorization and embedding via the `redis-om-spring-ai` module

## How Customers Use It

1. Run Redis 8+ or Redis Stack (JSON + Query Engine required).
2. Add `redis-om-spring` (and optionally `redis-om-spring-ai`) as a dependency.
3. Annotate domain classes with `@Document` or enhanced hash annotations.
4. Enable repositories with `@EnableRedisDocumentRepositories` or `@EnableRedisEnhancedRepositories`.
5. Use Spring repository interfaces, query derivation, `@Query`/`@Aggregation`, or `EntityStream`.

Redis defaults to `localhost:6379`. A `docker-compose.yml` at the repo root starts Redis Stack for local work.

## Supported Redis Targets

The library requires the Query Engine and JSON capabilities. This means:

- Redis 8+ (with bundled modules)
- Redis Stack
- Redis Enterprise
- Redis Cloud

Plain Redis OSS without modules does not support the full feature set.

## Source of Truth

Read in this order:

1. The active feature spec in `specs/<<slug>>/`
2. `README.md` — user-facing project overview and public API examples
3. `docs/content/modules/ROOT/pages/` — Antora documentation site source
4. This file

## Before Coding

1. Read the active feature spec, plan, and tasks.
2. Read `README.md` for the public API surface and user expectations.

Do not implement behavior changes without an approved spec.

## Module Map

| Module | Purpose |
|---|---|
| `redis-om-spring/` | Core library — annotations, repositories, indexing, search |
| `redis-om-spring-ai/` | Optional AI/vectorization extension (Spring AI + DJL) |
| `tests/` | **All integration and unit tests go here** — not inside the library modules |
| `docs/` | Antora documentation site + Javadoc generation |
| `demos/` | Sample Spring Boot applications |
| `specs/` | Feature specs (one folder per feature) |

Key config files: `gradle.properties`, `build.gradle`, `gradle/build-conventions.gradle`, `settings.gradle`.

## Best Demos to Run First

```bash
./gradlew :demos:roms-documents:bootRun      # JSON document mapping + REST CRUD
./gradlew :demos:roms-multitenant:bootRun    # Dynamic index naming, tenant isolation
./gradlew :demos:roms-hybrid:bootRun         # Hybrid text + vector search
./gradlew :demos:roms-vectorizers:bootRun    # Automatic embedding + semantic search
```

## New Feature Gate

Every new feature requires three approved artifacts before any implementation code is written:

1. `specs/<slug>/spec.md` — approved requirements and acceptance scenarios
2. `specs/<slug>/plan.md` — approved implementation design
3. `specs/<slug>/tasks.md` — approved execution checklist

**Do not write implementation code until all three are approved.**

Branch names must be **under 40 characters**: a short type prefix, a `/`, then the slug.

```
feat/730-sentinel-connection        ✓
fix/RED-1234-sentinel-connection    ✓
docs/update-vector-search-guide     ✓
chore/upgrade-jedis                 ✓
feat/add-a-very-long-description    ✗  too long
```

Common prefixes: `feat/`, `fix/`, `docs/`, `chore/`, `refactor/`, `test/`.

## Working Rules

- Stay within the approved spec.
- Keep changes focused and reviewable.
- Do not refactor unrelated code.
- Add or update tests when behavior changes.
- Tests go in `tests/src/test/java/com/redis/om/spring/` — never inside `redis-om-spring/` or `redis-om-spring-ai/`.
- Do not add dependencies without justification.
- Do not hard-code environment-specific values.
- Do not weaken, skip, or delete tests to force a change through.
- Update docs under `docs/content/modules/ROOT/pages/` when changing user-visible behavior.
- Search existing specs before introducing a new pattern.
- Do not invent folder layouts or architecture that are not in source-of-truth docs.
- **This repo has no Maven wrapper.** Use `./gradlew` only. Do not reference `mvnw`.

## Known Inconsistencies to Avoid Repeating

- Some older demo and docs files reference `mvnw` commands — ignore these, the repo is Gradle-only.
- The Java minimum requirement varies across docs: the build toolchain requires Java 21; some README text says Java 17. Treat **Java 21** as authoritative.

## When To Ask

Ask for clarification when:

- the spec is ambiguous on a critical point
- a decision affects scope, security, or user experience
- multiple valid approaches have different architectural consequences
- the proposed change conflicts with the spec or existing conventions

## Verification

Run before handoff:

```bash
./gradlew spotlessCheck build aggregateTestReport -S
```

Fix any formatting violations first:

```bash
./gradlew spotlessApply
```

If verification cannot run (e.g., no Redis available), say why clearly.

## Build Essentials

```bash
# Start Redis (required for integration tests)
docker compose up -d

# Fix formatting
./gradlew spotlessApply

# Full build + tests
./gradlew build aggregateTestReport -S

# Run a specific test class
./gradlew :tests:test --tests "com.redis.om.spring.FullClassName"

# Build without tests
./gradlew assemble -S
```

## Current Technical Baseline

- Language: Java 21 (toolchain in `gradle/build-conventions.gradle`)
- Framework: Spring Boot 4.0.0, Spring Data Redis 4.0.0
- Redis client: Jedis 7.x
- AI module: Spring AI 1.0.1, DJL 0.30.0
- Build: Gradle — versions in `gradle.properties`

## Test Strategy

The `tests/` module is the main validation surface. It contains:

- Unit tests
- Integration tests (Testcontainers-backed Redis)
- Annotation processor validation
- Coverage across document mapping, hash mapping, search, aggregations, vectorization, multitenancy, and indexing

Tests are **not** colocated inside the library modules — `tests/` is intentionally separate. Sentinel topology testing guidance is in `tests/README.md`.

## Documentation System

Built with Antora. Source lives under `docs/content/modules/ROOT/pages/`.

- Javadocs are generated during the docs build and copied into Antora attachments.
- Published to GitHub Pages via `.github/workflows/docs.yml`.
- Several reference sections are still incomplete — check `docs/README.md` for current status before assuming a topic is fully documented.

## CI Workflows

| File | Trigger | Purpose |
|---|---|---|
| `.github/workflows/build.yml` | Pull request | spotlessCheck + build + tests |
| `.github/workflows/early-access.yml` | Push to `main` | Snapshot publish via JReleaser |
| `.github/workflows/release.yml` | Manual `workflow_dispatch` | Official release (bumps version, publishes, triggers docs) |
| `.github/workflows/docs.yml` | Manual / release | Docs site publish to GitHub Pages |

Releasing is operator-driven via `workflow_dispatch` — do not bump `gradle.properties` version manually.

## Spec Workflow

Use `specs/` for feature work. Each feature gets its own numbered folder:

```text
specs/730-sentinel-connection/       # GH issue slug
specs/RED-1234-sentinel-connection/  # Jira ticket slug
specs/sentinel-connection/           # no tracker slug
└── each folder contains: spec.md, plan.md, tasks.md
```

The slug is the branch name minus the `feat/` or `fix/` prefix.

See [specs/README.md](specs/README.md) for the full convention.

## Important Links

- GitHub: https://github.com/redis/redis-om-spring
- Docs: https://redis.github.io/redis-om-spring/
- Maven Central (core): https://central.sonatype.com/artifact/com.redis.om/redis-om-spring
- Maven Central (AI): https://central.sonatype.com/artifact/com.redis.om/redis-om-spring-ai
- Issues: https://github.com/redis/redis-om-spring/issues

## Recent Changes

<!-- Append a one-liner per merged spec as features land -->
<!-- Format: - <slug>: what shipped -->
