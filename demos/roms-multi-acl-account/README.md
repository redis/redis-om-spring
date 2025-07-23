# 🔐 Multi-ACL Account Demo (Redis OM Spring)

This demo illustrates how to use **Redis ACLs** in a Spring Boot application with **Redis OM Spring** to separate read and write responsibilities across different Redis users.

## 🧪 What It Tests

- `userA` can **read and write** data.
- `userB` can **only read**.
- Attempts by `userB` to write throw a `NOPERM` error.

## 🔧 Redis ACL Configuration (Testcontainers)

```conf
user default off on >redispass allcommands allkeys

user userA on >passwordA +@all allkeys
user userB on >passwordB +@read +ping +ft.create allkeys
```

## 🐳 Running the Demo

This demo uses Testcontainers with Redis Open Source 8 and a custom ACL config. No external Redis setup is required.

To run the integration test:

./gradlew :demos:roms-multi-acl-account:test


## Notes
- Each Redis user is wired with a distinct JedisConnectionFactory, RedisModulesClient, and CustomRedisKeyValueTemplate.
- Read and write repositories are configured using @EnableRedisDocumentRepositories pointing to the appropriate templates.
- RedisSearch indexes must be created using a user with +ft.create permission.