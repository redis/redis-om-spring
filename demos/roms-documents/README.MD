# Redis OM Spring Demo - Documents

This demo showcases the use of Redis OM Spring's `@Document` annotation for storing and retrieving JSON documents in Redis.

## Requirements

- Java 21
- Redis Stack running on `localhost:6379`

From the repository root, start the bundled Redis Stack service with:

```bash
docker compose up -d redis
```

## Features

- Demonstrates how to use the `@Document` annotation to map Java objects to Redis JSON documents
- Shows how to create repositories that extend `RedisDocumentRepository`
- Provides examples of querying documents using method names and explicit `@Query` annotations
- Includes REST API endpoints for document read and query operations
- Includes an opt-in lexicographic ID range reproduction for large `@Indexed(sortable = true, lexicographic = true)` fields
- Shows `withSuffixTrie` and phonetic indexing on a `Company` text field

## Key Components

The demo includes these domain models:

- Company
- Person
- Event
- LexicographicIdRecord, used only by the opt-in lexicographic reproduction runner

Each primary model is mapped as a Redis JSON document and has corresponding repository and controller classes. The application seeds sample `Company` and `Person` data at startup.

## Running the Demo

From the repository root:

```bash
./gradlew :demos:roms-documents:bootRun
```

The application uses the default Spring Boot port, `8080`.

## API Endpoints

The demo exposes read/query endpoints under `/api`.

Company endpoints:

```text
GET /api/companies/all
GET /api/companies/all-ids
GET /api/companies/{id}
GET /api/companies/name/{name}
GET /api/companies/description/{description}
GET /api/companies/name/starts/{prefix}
GET /api/companies/employees/count/{count}
GET /api/companies/employees/range/{low}/{high}
GET /api/companies/near?lat={lat}&lon={lon}&d={distanceInMiles}
GET /api/companies/tags?tags={tag}
```

Person endpoints:

```text
GET /api/persons/all
GET /api/persons/all-ids
GET /api/persons/{id}
GET /api/persons/name/{last}/{first}
```

Event endpoints:

```text
GET /api/events/between?start={isoDateTime}&end={isoDateTime}
```

This demo does not currently configure Swagger/OpenAPI UI.

## Lexicographic ID Reproduction

The demo includes an opt-in runner that reproduces the large lexicographic ID range behavior for a field declared as:

```java
@Id
@Indexed(sortable = true, lexicographic = true)
private String id;
```

Quick run:

```bash
./gradlew :demos:roms-documents:bootRun --args='--spring.main.web-application-type=none --demo.lexicographic-repro.enabled=true --demo.lexicographic-repro.count=10000 --demo.lexicographic-repro.batch-size=1000 --demo.lexicographic-repro.update-count=100 --demo.lexicographic-repro.threshold=lex000000000'
```

Stronger signal:

```bash
./gradlew :demos:roms-documents:bootRun --args='--spring.main.web-application-type=none --demo.lexicographic-repro.enabled=true --demo.lexicographic-repro.count=50000 --demo.lexicographic-repro.batch-size=2000 --demo.lexicographic-repro.update-count=100 --demo.lexicographic-repro.threshold=lex000000000'
```

The runner has two diagnostic phases:

- Clears and seeds `LexicographicIdRecord` documents
- Rebuilds the lexicographic sorted set `lex-repro:id:lex`
- `WRITE-SIDE CLEANUP DEMO`: updates existing records so the lexicographic indexer has to clean old ID entries
- `MISSING QUERY FIX DEMO`: runs `LexicographicIdRecord$.ID.gt(threshold).limit(1)`
- Logs sorted set size, query time, and heap delta

What to look for in the write-side cleanup phase:

```text
WRITE-SIDE CLEANUP DEMO: older cleanup implementations used ZRANGE 0 -1 on lex-repro:id:lex once per update, materializing about 1000000 sorted-set members
WRITE-SIDE CLEANUP DEMO: update loop completed in 51 ms
WRITE-SIDE CLEANUP DEMO: sorted set size after updates is 10000
```

This shows why optimized write-side cleanup matters: a full-scan implementation scales with `sorted set size * update count`, while ID-field cleanup can remove exact lexicographic members directly.

Then look for:

```text
MISSING QUERY FIX DEMO: ID.gt(lex000000000) has 9999 lexicographic matches in a 10000 member sorted set
MISSING QUERY FIX DEMO: the current stream predicate still calls rangeByLex(..., Limit.unlimited()) before limit(1) is applied
MISSING QUERY FIX DEMO: ID.gt(lex000000000) with limit(1) returned 1 record(s)
```

This shows the remaining query-side issue: `limit(1)` returns one entity, but `ID.gt(...)` still materializes all matching lexicographic IDs first.

Useful properties:

```text
demo.lexicographic-repro.enabled=true
demo.lexicographic-repro.count=50000
demo.lexicographic-repro.batch-size=1000
demo.lexicographic-repro.update-count=100
demo.lexicographic-repro.threshold=lex000000000
```

## WITHSUFFIXTRIE and Phonetic Example

The `Company` model includes a searchable description field with suffix trie and phonetic indexing enabled:

```java
@Searchable(phonetic = "dm:en", withSuffixTrie = true)
private String description;
```

Run the demo and inspect the generated company index:

```bash
./gradlew :demos:roms-documents:bootRun
docker exec redis-om-spring-redis-1 redis-cli FT.INFO com.redis.om.documents.domain.CompanyIdx
```

The `description` attribute should include `WITHSUFFIXTRIE`:

```text
identifier
$.description
attribute
description
type
TEXT
WITHSUFFIXTRIE
```

Redis Search may not echo `PHONETIC` in `FT.INFO`. Check phonetic expansion with `FT.EXPLAINCLI`:

```bash
docker exec redis-om-spring-redis-1 redis-cli FT.EXPLAINCLI com.redis.om.documents.domain.CompanyIdx '@description:kash'
```

Expected plan shape:

```text
@description:UNION {
  @description:kash
  @description:<KX(expanded)
  @description:+kash(expanded)
}
```

You can also query the seeded `Company` data through the existing REST API:

```bash
curl http://localhost:8080/api/companies/description/kash
```

## Testing

The demo includes tests demonstrating how to use Redis OM Spring in a test environment.

```bash
./gradlew :demos:roms-documents:test
```
