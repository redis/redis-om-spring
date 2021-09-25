# spring-integration

## Features

### RedisHash Enhancements
- [ ] Override Secondary Index Implementations with RediSearch Indices

### Documents / JSON
- [ ] `@Reference` to other `@Document` or `@RedisHash` annotated models

### Search

- [✅] Implement RediSearch "native" indexing annotations (`@TextIndexed`, `@TagIndexed`, `@GeoIndexed`, `@NumericIndexed`)
- [ ] Implement `findByXXXX` methods for RediSearch indexed models
- [ ] Handle model return values for `@Query` and `@Aggregations` "native" annotations (add more tests)
- [ ] Add `@Indexable` and `@Searchable` high-level annotations
- [ ] Add Fluent/Stream API-like/Functional Query/Aggreggation Builder

### Probabilistic Data Structures

- [✅] Implement Bloom Filter methods for Repositories: `existByXXX` for `XXX` `@Bloom` annotated fields
- [ ] Review other PDS use cases

### Rate Limiting

- [ ] Design Rate Limiting Annotation for Controller?
- [ ] Determine 3rd party dependencies for Rate Limiting

### Session Management

- [ ] Research if this is already in Spring `@Session`

### Graph

- [ ] Basic annotations `@Node`, `@Property`, `@Relationship`, `@NodeReference`?

### Time Series Research

- [ ] Research APIs - Databases

### Caching Enhancements

- [ ] Research what module can do that OSS can't

### AI Use Cases

- [ ] Research use cases

### Leaderboards

- [ ] See https://github.com/agoragames/leaderboard (using TopK?)

### Streams

- [ ] Recreate this https://kafka.apache.org/28/documentation/streams/tutorial ?
