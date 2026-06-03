# Contributing to Redis OM Spring

Thank you for your interest in contributing to Redis OM Spring! We welcome contributions from the community — bug fixes, new features, documentation improvements, and demo additions are all appreciated.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Project Architecture](#project-architecture)
- [Development Workflow](#development-workflow)
- [Testing](#testing)
- [Code Style](#code-style)
- [Adding a Demo](#adding-a-demo)
- [Submitting Changes](#submitting-changes)
- [Reporting Issues](#reporting-issues)

## Getting Started

Before contributing, please:

1. Read the [README.md](README.md) to understand the project
2. Check existing [issues](https://github.com/redis/redis-om-spring/issues) and [pull requests](https://github.com/redis/redis-om-spring/pulls)
3. For significant changes, open an issue first to discuss your approach

## Development Setup

### Prerequisites

- **Java 21** or higher
- **Docker** (for running Redis locally)
- **Gradle wrapper** — no global Gradle install needed; use `./gradlew`

### Clone and Build

```bash
git clone https://github.com/redis/redis-om-spring.git
cd redis-om-spring
./gradlew build
```

### Start Redis

The test suite and demos require a Redis Stack instance (includes the Redis Query Engine and RedisJSON):

```bash
docker-compose up -d
```

This starts `redis/redis-stack` on port 6379. Alternatively, most tests use Testcontainers and will spin up Redis automatically.

## Project Architecture

The repository is a multi-module Gradle project:

```
redis-om-spring/          # Core library module
demos/                    # Runnable Spring Boot demo applications
  roms-documents/
  roms-hashes/
  roms-vss/
  ...                     # See demos/README.md for the full list
```

### Core Library

Key packages inside `redis-om-spring/src/main/java/com/redis/om/spring/`:

- **`annotations/`** — `@Document`, `@Indexed`, `@Searchable`, `@Vectorize`, etc.
- **`repository/`** — `RedisDocumentRepository`, `RedisEnhancedRepository`, and base classes
- **`ops/`** — Entity streams and fluent query DSL
- **`indexing/`** — Index creation and management (RediSearch schema generation)
- **`vectorize/`** — Embedding providers (OpenAI, Azure OpenAI, VertexAI, Bedrock, Transformers/DJL, Ollama)
- **`metamodel/`** — Annotation processor that generates `$`-prefixed metamodel classes

Annotation processing happens at compile time via the `redis-om-spring` module itself configured as an `annotationProcessor` dependency.

## Development Workflow

### 1. Create a Branch

```bash
git checkout -b fix/short-description
# or
git checkout -b feat/short-description
```

### 2. Make Your Changes

Edit source files under `redis-om-spring/src/`. Run the build:

```bash
./gradlew :redis-om-spring:build
```

### 3. Format Code

Apply the project's code style (Spotless + Eclipse formatter) before committing:

```bash
./gradlew spotlessApply
```

To check without modifying files:

```bash
./gradlew spotlessCheck
```

### 4. Run Tests

```bash
# Full test suite (requires Docker for Testcontainers)
./gradlew :redis-om-spring:test

# Specific test class
./gradlew :redis-om-spring:test --tests "com.redis.om.spring.search.stream.EntityStreamTest"

# Tests with verbose output
./gradlew :redis-om-spring:test --info
```

### 5. Common Commands

| Command | Description |
|---------|-------------|
| `./gradlew :redis-om-spring:build` | Compile and run all checks |
| `./gradlew :redis-om-spring:test` | Run the test suite |
| `./gradlew spotlessApply` | Auto-format all source files |
| `./gradlew spotlessCheck` | Check formatting without modifying |
| `./gradlew publishToMavenLocal` | Publish a snapshot for local demo testing |
| `./gradlew :demos:<name>:bootRun` | Run a specific demo |

## Testing

### Running Tests

Most tests use [Testcontainers](https://testcontainers.com/) to spin up a `redis/redis-stack` container automatically. Make sure Docker is running before executing the test suite.

```bash
# All tests
./gradlew :redis-om-spring:test

# One test class
./gradlew :redis-om-spring:test --tests "*.VectorSearchTest"
```

### Writing Tests

New features and bug fixes must include tests. Place test classes under:

```
redis-om-spring/src/test/java/com/redis/om/spring/
```

Guidelines:

- Extend `AbstractBaseDocumentTest` or `AbstractBaseEnhancedRedisTest` as appropriate — these set up Testcontainers and application context for you.
- Name test methods clearly: `givenX_whenY_thenZ` or a plain descriptive name.
- Test both the happy path and edge cases (empty results, null fields, large payloads).
- If your change affects index creation, include a test that verifies the RediSearch schema.

### Test Coverage

We aim to keep coverage high. New public APIs should have corresponding tests. You can generate a coverage report with:

```bash
./gradlew :redis-om-spring:test jacocoTestReport
# Report at: redis-om-spring/build/reports/jacoco/test/html/index.html
```

## Code Style

This project uses the **Spotless** Gradle plugin with an Eclipse formatter configuration. Key rules:

- **2-space indentation** (not 4)
- **KNR brace style** — opening braces at the end of the line
- **120-character line length**
- **Consistent import ordering**: `java.*`, `javax.*`, `org.*`, `com.*`, then others — no wildcards

Always run `./gradlew spotlessApply` before pushing. CI will fail on formatting violations.

Additional style guidelines:

- Use type hints / generics consistently
- Add Javadoc to all public types and methods — at minimum a one-line summary
- Prefer constructor injection over field injection in Spring beans
- Avoid raw types and unchecked casts; suppress with `@SuppressWarnings` only when genuinely necessary and add a comment explaining why

## Adding a Demo

Demos live in the `demos/` directory as independent Spring Boot modules.

1. Create a new subdirectory: `demos/roms-<your-feature>/`
2. Add a `build.gradle` — copy an existing one (e.g., `roms-documents/build.gradle`) as a starting point
3. Register it in the root `settings.gradle`:
   ```groovy
   include ':demos:roms-<your-feature>'
   ```
4. Write a `README.md` inside the demo directory describing:
   - What the demo demonstrates
   - Prerequisites (any env vars, data files)
   - How to run it: `./gradlew :demos:roms-<your-feature>:bootRun`
   - Key endpoints or usage examples
5. Add the demo to the table in [demos/README.md](demos/README.md)

## Submitting Changes

### Pull Request Checklist

- [ ] Branch is based on `main`
- [ ] All tests pass: `./gradlew :redis-om-spring:test`
- [ ] Code is formatted: `./gradlew spotlessApply`
- [ ] New/changed public APIs have Javadoc
- [ ] Tests added for new behavior
- [ ] `demos/README.md` updated if adding a demo

### Pull Request Description

- Use a clear, descriptive title referencing the issue if applicable (e.g., `fix: correct index creation for nested JSON arrays (#123)`)
- Describe **what** changed and **why**
- Include before/after examples for API changes

### Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) style:

```
<type>: <short summary>

[optional body]
```

Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`

Examples:
```
feat: add support for GEO_SHAPE field type
fix: correct FLAT index parameter for high-dimensional vectors
docs: add Javadoc to VectorQuery builder methods
test: add integration test for hybrid search scoring
```

Keep the first line under 72 characters. Reference issues with `(#123)` at the end.

## Reporting Issues

### Bug Reports

Please include:

1. **Environment**: Java version, Redis OM Spring version, Redis server version, OS
2. **Minimal reproducible example** — the smaller, the better
3. **Expected vs. actual behavior**
4. **Full stack trace**

### Feature Requests

Describe:

- The use case you're solving
- How you envision the API looking (code example)
- Any alternatives you considered

## Additional Resources

- [Redis OM Spring Documentation](https://redis.github.io/redis-om-spring/)
- [Redis Query Engine Documentation](https://redis.io/docs/latest/develop/interact/search-and-query/)
- [RedisJSON Documentation](https://redis.io/docs/latest/develop/data-types/json/)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Testcontainers](https://testcontainers.com/)

## Getting Help

- **GitHub Issues**: [redis/redis-om-spring/issues](https://github.com/redis/redis-om-spring/issues)
- **GitHub Discussions**: [redis/redis-om-spring/discussions](https://github.com/redis/redis-om-spring/discussions)
- **Discord**: [Redis Discord Server](https://discord.gg/redis)

## License

By contributing to Redis OM Spring you agree that your contributions will be licensed under the [MIT License](LICENSE).

Thank you for helping make Redis OM Spring better for everyone!
