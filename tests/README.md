# Redis OM Spring Tests

This directory contains all the tests for the Redis OM Spring library. The tests are organized by feature and include both unit tests and integration tests using TestContainers.

## Redis Sentinel Tests

The Redis Sentinel tests verify that Redis OM Spring works correctly with Redis Sentinel for high availability. These tests use TestContainers to spin up Redis Stack Server instances for testing.

### Implementation Notes

While a full Redis Sentinel setup would typically involve multiple Redis instances (master and replicas) along with Sentinel processes, for testing simplicity we use a single Redis Stack Server instance. This approach allows us to verify that:

1. Redis OM Spring's Sentinel configuration works correctly
2. The Redis OM Spring API functions properly when configured to use Redis Sentinel

In a separate validation (in the `broken_sentinel_test/dockerized-redis-oss-sentinel` directory), we have verified through manual testing that Redis Stack Server works correctly with Redis Sentinel, confirming that a full high-availability setup is possible.

### Test Classes

- `AbstractBaseDocumentSentinelTest` - Base test class that sets up a Redis Stack Server container
- `BasicSentinelTest` - Simple CRUD operations test
- `AdvancedSentinelTest` - Tests more advanced features like queries, filters, geospatial operations
- `SentinelConfigTest` - Tests the `SentinelConfig` configuration class

### Running the Tests

Tests can be run individually with:

```
mvn test -Dtest=com.redis.om.spring.annotations.document.sentinel.*
```

Note: The Sentinel tests are disabled in GitHub Actions by using the `@DisabledIfEnvironmentVariable` annotation since they require Docker.

## Production Use

For production use with Redis Sentinel, configure your Spring Boot application with:

```properties
spring.redis.sentinel.master=mymaster
spring.redis.sentinel.nodes=host1:26379,host2:26379,host3:26379
```

This will enable Redis OM Spring to connect through Sentinel for high availability.