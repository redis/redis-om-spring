<div align="center">
  <br/>
  <br/>
  <img width="360" src="docs/media/images/logo.svg" alt="Redis OM" />
  <br/>
  <br/>
</div>

<p><p align="center">Object Mapping (and more) for Redis!</p></p>

---

**Redis OM Spring** extends [Spring Data Redis](https://spring.io/projects/spring-data-redis) to take full advantage of Redis and [Redis Stack](https://reedis.io/docs/stack/).

| Stage                                             | Release                                      | Snapshot                                        | Issues                                                               | Resolution                                                                      | Code QL                                      | License                                  | SDR Ver.                                                |
|---------------------------------------------------| -------------------------------------------- | ----------------------------------------------- | -------------------------------------------------------------------- | ------------------------------------------------------------------------------- | -------------------------------------------- | ---------------------------------------- | ------------------------------------------------------- |
| [![Project stage][badge-stage]][badge-stage-page] | [![Releases][badge-releases]][link-releases] | [![Snapshots][badge-snapshots]][link-snapshots] | [![Percentage of issues still open][badge-open-issues]][open-issues] | [![Average time to resolve an issue][badge-issue-resolution]][issue-resolution] | [![CodeQL][badge-codeql]][badge-codeql-page] | [![License][license-image]][license-url] | [![SDR Version][sdr-badge-releases]][sdr-link-releases] |

Learn / Discuss / Collaborate

| Discord                                   | Twitch                                 | YouTube                                   | Twitter                                   |
| ----------------------------------------- | -------------------------------------- | ----------------------------------------- | ----------------------------------------- |
| [![Discord][discord-shield]][discord-url] | [![Twitch][twitch-shield]][twitch-url] | [![YouTube][youtube-shield]][youtube-url] | [![Twitter][twitter-shield]][twitter-url] |

<details>
  <summary><strong>Table of contents</strong></summary>

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [💡 Why Redis OM?](#-why-redis-om)
- [🍀 Redis OM Spring](#-redis-om-spring)
- [🏁 Getting Started](#-getting-started)
  - [🚀 Launch Redis](#-launch-redis)
  - [The SpringBoot App](#the-springboot-app)
  - [💁‍♂️ The Mapped Model](#-the-mapped-model)
  - [🧰 The Repository](#-the-repository)
  - [🚤 Querying with Entity Streams](#-querying-with-entity-streams)
    - [👭 Entity Meta-model](#-entity-meta-model)
- [💻 Maven configuration](#-maven-configuration)
  - [Official Releases](#official-releases)
    - [Explicitly configuring OM as an annotation processor](#explicitly-configuring-om-as-an-annotation-processor)
  - [Snapshots](#snapshots)
- [🐘 Gradle configuration](#-gradle-configuration)
  - [Add Repository - Snapshots Only](#add-repository---snapshots-only)
  - [Dependency](#dependency)
- [📚 Documentation](#-documentation)
- [Demos](#demos)
  - [Embedded Demos](#embedded-demos)
  - [External Demos](#external-demos)
- [⛏️ Troubleshooting](#-troubleshooting)
- [✨ So How Do You Get RediSearch and RedisJSON?](#-so-how-do-you-get-redisearch-and-redisjson)
- [💖 Contributing](#-contributing)
- [🧑‍🤝‍🧑 Sibling Projects](#-sibling-projects)
- [📝 License](#-license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

</details>

## 💡 Why Redis OM?

The Redis OM family of client libraries provide high-level abstractions, idiomatically implemented for your language and platform of choice. We currently cater to the Node, Python, .NET, and Spring communities.

## 🍀 Redis OM Spring

Redis OM Spring provides powerful repository and custom object-mapping abstractions built on top of the Spring Data Redis ([SDR](https://spring.io/projects/spring-data-redis)) framework.

This **preview** release provides all Spring Data Redis, plus:

* `@Document` annotation to map Spring Data models to Redis JSON documents
* Enhancement to the Spring Data Redis `@RedisHash` via `@EnableRedisEnhancedRepositories`:
  - uses Redis' native search engine (RediSearch) for secondary indexing
  - uses [ULID](https://github.com/ulid/spec) for `@Id` annotated fields
* `RedisDocumentRepository` with automatic implementation of Repository interfaces for complex querying capabilities using `@EnableRedisDocumentRepositories`
* Declarative search indexes via `@Indexed`
* Full-text search indexes via `@Searchable`
* `EntityStream`s: Streams-based Query and Aggregations Builder
* `@Bloom` annotation to determine very fast, with and with high degree of certainty, whether a value is in a collection.
* `@Vectorize` annotation to generate embeddings for text and images for use in Vector Similarity Searches
* Vector Similarity Search API (See [Redis Stack Vectors](https://redis.io/docs/stack/search/reference/vectors/))

**Note:** Redis OM Spring depends on Jedis.

## 🏁 Getting Started

Here is a quick teaser of an application using Redis OM Spring to map a Spring Data model
using a RedisJSON document.

### 🚀 Launch Redis

Redis OM Spring relies on the search, query, and JSON capabilities of [Redis Stack](https://reedis.io/docs/stack/).
Before writing any code, you'll need a Redis Stack. The quickest way to get
this is with Docker:

```sh
docker run -p 6379:6379 -p 8001:8001 redis/redis-stack
```

This launches [redis-stack](https://redis.io/docs/stack/), an extension of Redis that adds several modern data
structures to Redis. You'll also notice that if you open up `http://localhost:8001`, you'll have access to the
redis-insight GUI, a GUI you can use to visualize and work with your data in Redis.
We have also provided a Docker Compose YAML file for you to quickly get started
using [Redis Stack](https://redis.io/docs/stack/).

To launch the docker compose application, on the command line (or via Docker Desktop), clone this repository and run
(from the root folder):

```bash
docker compose up
```

### Configuring your Redis Connection

By default, Redis OM Spring connects to `localhost` at port `6379`. If
your instance is running somewhere else, you can configure the connection
in your `application.properties` or `application.yaml`:

In `application.properties`:

```
spring.data.redis.host=your.cloud.db.redislabs.com
spring.data.redis.port=12345
spring.data.redis.password=xxxxxxxx
spring.data.redis.username=default
```

In `application.yaml`:

```
spring:
  data:
    redis:
      host: your.cloud.db.redislabs.com
      port: 12345
      password: xxxxxxxx
      username: default
```

### The SpringBoot App

Use the `@EnableRedisDocumentRepositories` annotation to scan for `@Document` annotated Spring models,
Inject repositories beans implementing `RedisDocumentRepository` which you can use for CRUD operations and custom queries (all by declaring Spring Data Query Interfaces):

```java
package com.redis.om.documents;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.geo.Point;

import com.redis.om.documents.domain.Company;
import com.redis.om.documents.repositories.CompanyRepository;

@SpringBootApplication
@Configuration
@EnableRedisDocumentRepositories(basePackages = "com.redis.om.documents.*")
public class RomsDocumentsApplication {

  @Autowired
  CompanyRepository companyRepo;

  @Bean
  CommandLineRunner loadTestData() {
    return args -> {
      // remove all companies
      companyRepo.deleteAll();

      // Create a couple of `Company` domain entities
      Company redis = Company.of(
        "Redis", "https://redis.com", new Point(-122.066540, 37.377690), 526, 2011 //
      );
      redis.setTags(Set.of("fast", "scalable", "reliable"));

      Company microsoft = Company.of(
        "Microsoft", "https://microsoft.com", new Point(-122.124500, 47.640160), 182268, 1975 //
      );
      microsoft.setTags(Set.of("innovative", "reliable"));

      // save companies to the database
      companyRepo.save(redis);
      companyRepo.save(microsoft);
    };
  }

  public static void main(String[] args) {
    SpringApplication.run(RomsDocumentsApplication.class, args);
  }
}
```

### 💁‍♂️ The Mapped Model

Like many other Spring Data projects, an annotation at the class level determines how instances
of the class are persisted. Redis OM Spring provides the `@Document` annotation to persist models as JSON documents using RedisJSON:

```java
package com.redis.om.documents.domain;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Searchable;
import lombok.*;

@Data @NoArgsConstructor @RequiredArgsConstructor(staticName = "of") @AllArgsConstructor(access = AccessLevel.PROTECTED) @Document public class Company {
  @Id private String id;
  @Searchable private String name;
  @Indexed private Point location;
  @Indexed private Set<String> tags = new HashSet<>();
  @Indexed private Integer numberOfEmployees;
  @Indexed private Integer yearFounded;
  private String url;
  private boolean publiclyListed;

  // ...
}
```

Redis OM Spring, replaces the conventional `UUID` primary key strategy generation with a `ULID` (Universally Unique Lexicographically Sortable Identifier) which is faster to generate and easier on the eyes.

### 🧰 The Repository

Redis OM Spring data repository's goal, like other Spring Data repositories, is to significantly reduce the amount of boilerplate code required to implement data access. Simply create a Java interface
that extends `RedisDocumentRepository` that takes the domain class to manage as well as the ID type of the domain class as type arguments. `RedisDocumentRepository` extends the Spring Data class `PagingAndSortingRepository`.

Declare query methods on the interface. You can both, expose CRUD methods or create declarations for complex queries that Redis OM Spring will fulfill at runtime:

```java
package com.redis.om.documents.repositories;

import java.util.*;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.repository.query.Param;

import com.redis.om.documents.domain.Company;
import com.redis.om.spring.annotations.Query;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface CompanyRepository extends RedisDocumentRepository<Company, String> {
  // find one by property
  Optional<Company> findOneByName(String name);

  // geospatial query
  Iterable<Company> findByLocationNear(Point point, Distance distance);

  // find by tag field, using JRediSearch "native" annotation
  @Query("@tags:{$tags}")
  Iterable<Company> findByTags(@Param("tags") Set<String> tags);

  // find by numeric property
  Iterable<Company> findByNumberOfEmployees(int noe);

  // find by numeric property range
  Iterable<Company> findByNumberOfEmployeesBetween(int noeGT, int noeLT);

  // starting with/ending with
  Iterable<Company> findByNameStartingWith(String prefix);
}
```

The repository proxy has two ways to derive a store-specific query from the method name:

- By deriving the query from the method name directly.
- By using a manually defined query using the `@Query` or `@Aggregation` annotations.

### 🚤 Querying with Entity Streams

Redis OM Spring Entity Streams provides a Java 8 Streams interface to Query Redis JSON documents using RediSearch. Entity Streams allow you to process data in a type safe declarative way similar to SQL statements. Streams can be used to express a query as a chain of operations.

Entity Streams in Redis OM Spring provides the same semantics as Java 8 streams. Streams can be made of Redis Mapped entities (`@Document`) or one or more properties of an Entity. Entity Streams progressively build the query until a terminal operation is invoked (such as `collect`). Whenever a Terminal operation is applied to a Stream, the Stream cannot accept additional operations to its pipeline and it also means that the Stream is started.

Let's start with a simple example, a Spring `@Service` which includes `EntityStream` to query for instances of the mapped class `Person`:

```java
package com.redis.om.skeleton.services;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.redis.om.skeleton.models.Person;
import com.redis.om.skeleton.models.Person$;
import com.redis.om.spring.search.stream.EntityStream;

@Service
public class PeopleService {
  @Autowired
  EntityStream entityStream;

  // Find all people
  public Iterable<Person> findAllPeople() {
    return entityStream //
        .of(Person.class) //
        .collect(Collectors.toList());
  }

}
```

The `EntityStream` is injected into the `PeopleService` using `@Autowired`. We can then get a stream for `Person` objects by using `entityStream.of(Person.class)`. At this point the stream represents the equivalent of a `SELECT * FROM Person` on a relational database. The call to `collect` will then execute the underlying query and return a collection of all `Person` objects in Redis.

#### 👭 Entity Meta-model

To produce more elaborate queries, you're provided with a generated metamodel, which is a class with the same name as your model but ending with a dollar sign. In the
example below, our entity model is `Person` therefore we get a metamodel named `Person$`. With the metamodel you have access to the operations related to the
underlying search engine field. For example, in the example we have an `age` property which is an integer. Therefore, our metamodel has an `AGE` property which has
numeric operations we can use with the stream's `filter` method such as `between`.

```java
// Find people by age range
public Iterable<Person> findByAgeBetween(int minAge, int maxAge) {
  return entityStream //
      .of(Person.class) //
      .filter(Person$.AGE.between(minAge, maxAge)) //
      .sorted(Person$.AGE, SortOrder.ASC) //
      .collect(Collectors.toList());
}
```

In this example we also make use of the Streams `sorted` method to declare that our stream will be sorted by the `Person$.AGE` in `ASC`ending order.

Check out the full set of tests for [EntityStreams](https://github.com/redis/redis-om-spring/tree/main/redis-om-spring/src/test/java/com/redis/om/spring/search/stream)

### 👯‍️ Querying by Example (QBE)

Query by Example (QBE) is a user-friendly querying technique with a simple interface. It allows dynamic query creation
and does not require you to write queries that contain field names. In fact, Query by Example does not require you to
write queries by using store-specific query languages at all.

#### QBE Usage

The Query by Example API consists of four parts:
* **Probe**: The actual example of a domain object with populated fields.
* **ExampleMatcher**: The `ExampleMatcher` carries details on how to match particular fields. It can be reused across multiple `Examples`.
* **Example**: An Example consists of the probe and the ExampleMatcher. It is used to create the query.
* **FetchableFluentQuery**: A `FetchableFluentQuery` offers a fluent API, that allows further customization of a query derived from an `Example`.
  Using the fluent API lets you specify ordering projection and result processing for your query.

Query by Example is well suited for several use cases:

* Querying your data store with a set of static or dynamic constraints.
* Frequent refactoring of the domain objects without worrying about breaking existing queries.
* Working independently of the underlying data store API.

For example, if you have an `@Document` or `@RedisHash` annotated entity you can create an instance, partially populate its
properties, create an `Example` from it, and used the `findAll` method to query for similar entities:

```java
MyDoc template = new MyDoc();
template.setTitle("hello world");
template.setTag(Set.of("artigo"));

Example<MyDoc> example = Example.of(template, ExampleMatcher.matchingAny());

Iterable<MyDoc> allMatches = repository.findAll(example);
```

## 💻 Maven configuration

### Official Releases

```xml
<dependency>
  <groupId>com.redis.om</groupId>
  <artifactId>redis-om-spring</artifactId>
  <version>${version}</version>
</dependency>
```

#### Explicitly configuring OM as an annotation processor

For Maven, things normally just work, when you run `./mvnw spring-boot:run`. Some users have experienced this not being
the case, in which I recommend to explicitly declaring the `maven-compiler-plugin` in the case below it is paired with
an app created with [`start.spring.io`](https://start.spring.io/) with Spring Boot `v3.0.4` (all other versions can be
inherited from the parent poms):

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>${maven-compiler-plugin.version}</version>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-configuration-processor</artifactId>
        <version>3.1.2</version>
      </path>
      <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
      </path>
      <path>
        <groupId>com.redis.om</groupId>
        <artifactId>redis-om-spring</artifactId>
        <version>0.8.8-SNAPSHOT</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

### Snapshots

```xml
  <repositories>
    <repository>
      <id>snapshots-repo</id>
      <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
    </repository>
  </repositories>
```

and

```xml
<dependency>
  <groupId>com.redis.om</groupId>
  <artifactId>redis-om-spring</artifactId>
  <version>${version}-SNAPSHOT</version>
</dependency>
```

**Ready to learn more?** Check out the [getting started](docs/getting_started.md) guide.

## 🐘 Gradle configuration

### Add Repository - Snapshots Only

```groovy
repositories {
    mavenCentral()
    maven {
        url 'https://s01.oss.sonatype.org/content/repositories/snapshots/'
    }
}
```

### Dependency
```groovy
ext {
  redisOmVersion = '0.8.8-SNAPSHOT'
}

dependencies {
  implementation "com.redis.om:redis-om-spring:$redisOmVersion"
  annotationProcessor "com.redis.om:redis-om-spring:$redisOmVersion"
}
```

## 📚 Documentation

The Redis OM documentation is available [here](docs/index.md).

## Demos

### Embedded Demos

These can be found in the `/demos` folder:

- **roms-documents**:
  - Simple API example of `@Document` mapping, Spring Repositories and Querying.
  - Run with  `./mvnw install -Dmaven.test.skip && ./mvnw spring-boot:run -pl demos/roms-documents`

- **roms-hashes**:
  - Simple API example of `@RedisHash`, enhanced secondary indices and querying.
  - Run with  `./mvnw install -Dmaven.test.skip && ./mvnw spring-boot:run -pl demos/roms-hashes`

- **roms-permits**:
  - Port of [Elena Kolevska's](https://github.com/elena-kolevska) Quick Start: Using RediSearch with JSON [Demo][redisearch-json] to Redis OM Spring.
  - Run with  `./mvnw install -Dmaven.test.skip && ./mvnw spring-boot:run -pl demos/roms-permits`

- **roms-vss**:
  - Port of [Redis Vector Search Demo](https://github.com/RedisVentures/redis-product-search).
  - Run with  `./mvnw install -Dmaven.test.skip && ./mvnw spring-boot:run -pl demos/roms-vss`

### External Demos

- **redis-om-spring-skeleton-app**:
  - Redis OM Spring Skeleton App
  - Repo: https://github.com/redis-developer/redis-om-spring-skeleton-app

- **redis-om-spring-react-todomvc**:
  - Redis OM Spring to build a RESTful API that satisfies the simple web API spec set by
    the Todo-Backend project using JSON Documents stored in Redis.
  - Repo: https://github.com/redis-developer/redis-om-spring-react-todomvc

- **redis-om-autocomplete-demo**:
  - A Spring Boot demo of autocomplete functionality using Redis OM Spring.
  - Repo: https://github.com/redis-developer/redis-om-autocomplete-demo

## ⛏️ Troubleshooting

If you run into trouble or have any questions, we're here to help!

First, check the [FAQ](docs/faq.md). If you don't find the answer there,
hit us up on the [Redis Discord Server](http://discord.gg/redis).

## ✨ So How Do You Get RediSearch and RedisJSON?

Redis OM relies on two source available Redis modules: [RediSearch][redisearch-url] and [RedisJSON][redis-json-url].

You can run these modules in your self-hosted Redis deployment, or you can use [Redis Enterprise][redis-enterprise-url], which includes both modules.

To learn more, read [our documentation](docs/redis_modules.md).

## 💖 Contributing

We'd love your contributions!

**Bug reports** are especially helpful at this stage of the project. [You can open a bug report on GitHub](https://github.com/redis-om/redis-om-spring/issues/new).

You can also **contribute documentation** -- or just let us know if something needs more detail. [Open an issue on GitHub](https://github.com/redis-om/redis-om-spring/issues/new) to get started.

## 🧑‍🤝‍🧑 Sibling Projects

- [Redis OM Node.js](https://github.com/redis/redis-om-node)
- [Redis OM Python](https://github.com/redis/redis-om-python)
- [Redis OM .NET](https://github.com/redis/redis-om-dotnet)

## 📝 License

Redis OM uses the [MIT license][license-url].

<!-- Badges / Shields -->

[ci-url]: https://github.com/redis-developer/redis-om-spring/actions/workflows/ci.yml
[badge-stage]: https://img.shields.io/badge/Project%20Stage-Development-green.svg
[badge-stage-page]: https://github.com/redis/redis-om-spring/wiki/Project-Stages
[badge-snapshots]: https://img.shields.io/nexus/s/https/s01.oss.sonatype.org/com.redis.om/redis-om-spring.svg
[badge-releases]: https://img.shields.io/maven-central/v/com.redis.om/redis-om-spring
[badge-open-issues]: http://isitmaintained.com/badge/open/redis/redis-om-spring.svg
[badge-issue-resolution]: http://isitmaintained.com/badge/resolution/redis/redis-om-spring.svg
[badge-codeql]: https://github.com/redis/redis-om-spring/actions/workflows/codeql-analysis.yml/badge.svg
[badge-codeql-page]: https://github.com/redis/redis-om-spring/actions/workflows/codeql-analysis.yml
[license-image]: https://img.shields.io/github/license/redis/redis-om-spring
[license-url]: LICENSE
[sdr-badge-releases]: https://img.shields.io/maven-central/v/org.springframework.data/spring-data-redis/3.1.2
[discord-shield]: https://img.shields.io/discord/697882427875393627?style=social&logo=discord
[twitch-shield]: https://img.shields.io/twitch/status/redisinc?style=social
[twitter-shield]: https://img.shields.io/twitter/follow/redisinc?style=social
[youtube-shield]: https://img.shields.io/youtube/channel/views/UCD78lHSwYqMlyetR0_P4Vig?style=social

<!-- Links -->

[redis-om-website]: https://developer.redis.com
[redis-om-python]: https://github.com/redis-om/redis-om-python
[redis-om-js]: https://github.com/redis-om/redis-om-js
[redis-om-dotnet]: https://github.com/redis-om/redis-om-dotnet
[redisearch-url]: https://oss.redis.com/redisearch/
[redis-json-url]: https://oss.redis.com/redisjson/
[ulid-url]: https://github.com/ulid/spec
[redis-enterprise-url]: https://redis.com/try-free/
[link-snapshots]: https://s01.oss.sonatype.org/content/repositories/snapshots/com/redis/om/redis-om-spring/
[link-releases]: https://repo1.maven.org/maven2/com/redis/om/redis-om-spring/
[open-issues]: http://isitmaintained.com/project/redis/redis-om-spring
[issue-resolution]: http://isitmaintained.com/project/redis/redis-om-spring
[redisearch-json]: https://github.com/redislabs-training/mod-devcap-redisjson-getting-started/blob/master/articles/QuickStart-RediSearchWithJSON.md
[sdr-link-releases]: https://repo1.maven.org/maven2/org/springframework/data/spring-data-redis/3.0.1/
[discord-url]: http://discord.gg/redis
[twitch-url]: https://www.twitch.tv/redisinc
[twitter-url]: https://twitter.com/redisinc
[youtube-url]: https://www.youtube.com/redisinc




