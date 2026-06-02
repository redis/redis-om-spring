# Redis OM Spring Demos

This directory contains runnable Spring Boot demo applications that showcase Redis OM Spring features.
Each demo is a self-contained module that can be built and run independently.

## Prerequisites

- Java 21+
- Docker (for running Redis locally)
- Gradle wrapper (`./gradlew`) — no global Gradle installation required

### Start Redis

```bash
docker-compose up -d
```

This starts a `redis/redis-stack` instance on port 6379, which includes the Redis Query Engine and RedisJSON modules.

## Running a Demo

From the **project root**, run any demo with:

```bash
./gradlew :demos:<demo-name>:bootRun
```

For example:

```bash
./gradlew :demos:roms-documents:bootRun
```

## Available Demos

| Demo | Description | Run Command |
|------|-------------|-------------|
| [roms-documents](roms-documents/README.md) | JSON document storage and querying using `@Document` | `./gradlew :demos:roms-documents:bootRun` |
| [roms-hashes](roms-hashes/README.md) | Hash-based models with secondary indexes via `@RedisHash` and `@Indexed` | `./gradlew :demos:roms-hashes:bootRun` |
| [roms-permits](roms-permits/README.md) | RediSearch + RedisJSON quick-start port (permit management) | `./gradlew :demos:roms-permits:bootRun` |
| [roms-modeling](roms-modeling/README.md) | Domain entity modeling patterns and Spring repositories | `./gradlew :demos:roms-modeling:bootRun` |
| [roms-vss](roms-vss/README.md) | Vector similarity search for fashion product recommendations | `./gradlew :demos:roms-vss:bootRun` |
| [roms-vss-movies](roms-vss-movies/README.md) | Movie recommendation system using vector similarity search (Redis 8) | `./gradlew :demos:roms-vss-movies:bootRun` |
| [roms-hybrid](roms-hybrid/README.md) | Hybrid search combining full-text (BM25) and vector similarity search | `./gradlew :demos:roms-hybrid:bootRun` |
| [roms-vectorizers](roms-vectorizers/README.md) | Automatic embedding generation and vector search via built-in vectorizers | `./gradlew :demos:roms-vectorizers:bootRun` |
| [roms-multitenant](roms-multitenant/README.md) | Multi-tenant SaaS with dynamic indexing and complete data isolation | `./gradlew :demos:roms-multitenant:bootRun` |
| [roms-multi-acl-account](roms-multi-acl-account/README.md) | Separate read/write Redis users using ACL-based multi-account setup | `./gradlew :demos:roms-multi-acl-account:bootRun` |
| [roms-amr-entraid](roms-amr-entraid/README.md) | Azure Managed Redis authentication via Microsoft Entra ID | `./gradlew :demos:roms-amr-entraid:bootRun` |

## Postman Collection

A Postman collection covering the REST endpoints of most demos is available at
[`redis-om-spring.postman_collection.json`](redis-om-spring.postman_collection.json).
Import it into Postman to explore the APIs interactively.

## Building All Demos

To compile all demos without running them:

```bash
./gradlew :demos:build
```

## Notes

- Demos use `mavenLocal()` first, so you can test against a locally built snapshot of `redis-om-spring` by running `./gradlew publishToMavenLocal` from the project root.
- Each demo's `build.gradle` pulls the `redis-om-spring` dependency from the composite build, so changes to the library are reflected immediately without a separate publish step.
