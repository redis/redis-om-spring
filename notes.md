- Annotation in Model
- Interceps the save method of the repo saving said model
- Does the bloom filter thing

- The Redis Developer family of libraries also provide robust implementations of
  - rate limiters,
  - leaderboards,
  - streams,
  - and distributed locks, to name just a few.


  The first tier works with any Redis instance, regardless of where it's hosted. Functionality for the first tier includes:

* Caching. This is by far the biggest Redis use case. Developers should reach for these libraries whenever they need to cache anything. This is how we get developers in the door.
* Rate Limiting. This is an easy win. Provide the high-level rate limiting abstraction so that the developers don't have to think about how to build this.
* Leaderboards. Another easy win, just like rate limiting. This way, developers don't have to learn anything about sorted sets.
* Streams. Provide a fluent stream abstraction with object modeling.

The second tier of functionality requires either Redis Enterprise or Redis Cloud. The most urgent of these is document/object modeling. Ideally, we should release this with the GA of RediSearch/RedisJSON.

* Document/Object modeling (using RedisJSON and Redisearch).
* Advanced Caching. Write-behind, read-through, etc. Requires Gears.
* Time Series (uses RedisTimeSeries). A high-level time series object. Requires no knowledge of time series commands.
* Full-text search indexes. Access to full-text search without knowledge of any RediSeach commands.
Graphs.
* De-duplicators. High-level abstractions for RedisBloom, for example.



Monggo docs are org.bson.Document

https://www.programmersought.com/article/3124546009/

Do these work in Spring Data Redis?

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

======== Spring Day Repository Registar

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;



RedisRepositoryConfigurationExtension

https://programmingsharing.com/most-used-spring-data-mongodb-annotations-589963d2ec34



========== Spring Composable Repositories

- https://www.baeldung.com/spring-data-composable-repositories
- org.springframework.data.repository.core.support.RepositoryComposition.RepositoryFragments used in Spring Data Redis
- Spring 5, we have the option to enrich our repositories with multiple fragment repositories.
- Requirement remains that we have these fragments as interface-implementation pairs.
- Intf CustomARepo, CustomBRepo w/ method signatures, then aggregate with `interface MyRepository extends JpaRepository<A, Long>, CustomARepo, CustomBRepo`
- Spring Data repositories are implemented by using fragments that form a repository composition.
- Fragments are the base repository, functional aspects (such as QueryDsl), and custom interfaces along with their implementations.
- Each time you add an interface to your repository interface, you enhance the composition by adding a fragment.
- The base repository and repository aspect implementations are provided by each Spring Data module.


=========== Customizable Things in Spring Data Redis
- Customizing Type Mapping - @TypeAlias("pers")
-




=========== Possible Steps
- Create custom redis serializer org.springframework.data.redis.serializer
- Spring Data Redis Enable repos return org.springframework.data.repository.config.DefaultRepositoryBaseClass for repositoryBaseClass()
  - DefaultRepositoryBaseClass: Placeholder class to be used in @Enable annotation's repositoryBaseClass attribute.
  - The configuration evaluation infrastructure can use this type to find out no special repository base class was configured and apply defaults.



https://stackoverflow.com/questions/67132894/what-is-springboot-alternative-to-javaee-cdi


KAIZEN ==> Change for the better, even if it slows you down


org.springframework.beans.factory.BeanDefinitionStoreException: Invalid bean definition with name 'redisKeyValueAdapter': @Bean definition illegally overridden by existing bean definition: Root bean: class [org.springframework.data.redis.core.RedisKeyValueAdapter];


spring.main.allow-bean-definition-overriding=true


=====

 GsonAutoConfiguration matched:
      - @ConditionalOnClass found required class 'com.google.gson.Gson' (OnClassCondition)

   GsonAutoConfiguration#gson matched:
      - @ConditionalOnMissingBean (types: com.google.gson.Gson; SearchStrategy: all) did not find any beans (OnBeanCondition)

   GsonAutoConfiguration#gsonBuilder matched:
      - @ConditionalOnMissingBean (types: com.google.gson.GsonBuilder; SearchStrategy: all) did not find any beans (OnBeanCondition)


=======

https://gist.github.com/bsbodden/39243ae89efa554b41f54401c05a4454

https://github.com/OsokinAlexander/infinispan-spring-repository

https://habr.com/ru/post/535218/

======= Recipes Collections

- https://medium.com/capital-one-tech/the-swiss-army-knife-and-the-cookbook-part-2-recipes-for-common-redis-patterns-1125ee2c8c96
- https://dzone.com/articles/meet-top-k-an-awesome-probabilistic-addition-to-re
- https://redislabs.com/blog/the-top-3-game-changing-redis-use-cases/
- Caching Demo - https://github.com/redislabs-training/demo-caching
- RedisGears Write Behind recipe for each module
  - Time Series
  -
- Retail
  - https://redislabs.com/blog/redismart-retail-application-with-redis/
- Chat

======

- https://www.baeldung.com/spring-show-all-beans
- https://www.baeldung.com/spring-data-jpa-query
- https://stackoverflow.com/questions/14266089/how-to-retrieve-spring-data-repository-instance-for-given-domain-class

-
==============


//Annotation to customize the query creator type to be used for a specific store.
import org.springframework.data.keyvalue.repository.config.QueryCreatorType;


@QueryCreatorType(RedisQueryCreator.class)
<<== value is `Class<? extends AbstractQueryCreator<?,?>>`
org.springframework.data.repository.query.parser Class AbstractQueryCreator<T,S>

Has an optional value `repositoryQueryType` which is a `Class<? extends RepositoryQuery>`



public @interface EnableRedisRepositories {


package org.springframework.data.redis.repository.query;
public class RedisQueryCreator extends AbstractQueryCreator<KeyValueQuery<RedisOperationChain>, RedisOperationChain> {


package org.springframework.data.repository.query.parser;
public abstract class AbstractQueryCreator<T, S> {


========

org.springframework.data.keyvalue.repository.query.KeyValuePartTreeQuery.createQuery(KeyValuePartTreeQuery.java:207)
	at org.springframework.data.keyvalue.repository.query.KeyValuePartTreeQuery.prepareQuery(KeyValuePartTreeQuery.java:149)

at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:344)

org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor


==========

Query query = new Query();
query.addCriteria(Criteria.where("name").is("Eric"));
List<User> users = mongoTemplate.find(query, User.class);

Query query = new Query();
query.addCriteria(Criteria.where("name").regex("^A"));
List<User> users = mongoTemplate.find(query,User.class)

Query query = new Query();
query.addCriteria(Criteria.where("name").regex("c$"));
List<User> users = mongoTemplate.find(query, User.class);

Query query = new Query();
query.addCriteria(Criteria.where("age").lt(50).gt(20));
List<User> users = mongoTemplate.find(query,User.class);

Query query = new Query();
query.with(Sort.by(Sort.Direction.ASC, "age"));
List<User> users = mongoTemplate.find(query,User.class);

final Pageable pageableRequest = PageRequest.of(0, 2);
Query query = new Query();
query.with(pageableRequest);

Repo
====

public interface UserRepository
  extends MongoRepository<User, String>, QueryDslPredicateExecutor<User> {
    ...
}

FindByX
=======
List<User> findByName(String name);
List<User> users = userRepository.findByName("Eric");

StartingWith and endingWith.
============================
List<User> findByNameStartingWith(String regexp);
List<User> findByNameEndingWith(String regexp);
List<User> users = userRepository.findByNameStartingWith("A");
List<User> users = userRepository.findByNameEndingWith("c");

Between
========
List<User> findByAgeBetween(int ageGT, int ageLT);
List<User> users = userRepository.findByAgeBetween(20, 50);

Like and OrderBy
================
List<User> users = userRepository.findByNameLikeOrderByAgeAsc("A");

@Query
======
@Query("{ 'name' : ?0 }")
List<User> findUsersByName(String name);

@Query("{ 'name' : { $regex: ?0 } }")
List<User> findUsersByRegexpName(String regexp); ==> List<User> users = userRepository.findUsersByRegexpName("^A");

@Query("{ 'age' : { $gt: ?0, $lt: ?1 } }")
List<User> findUsersByAgeBetween(int ageGT, int ageLT);

QueryDSL and Mongo
==================
Requires a code generation step to create Q classes


https://www.kumowiz.com/blogs/geospatial

https://www.baeldung.com/mongodb-geospatial-support

https://www.baeldung.com/mongodb-tagging

Box
Circle
Distance
Point
Polygon

```
package com.redis.spring;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.mapping.RedisMappingContext;

import com.redis.spring.annotations.Bloom;
import com.redis.spring.annotations.Document;
import com.redis.spring.annotations.TagIndexed;
import com.redis.spring.annotations.TextIndexed;
import com.redis.spring.client.RedisModulesClient;
import com.redis.spring.ops.RedisModulesOperations;
import com.redis.spring.ops.json.JSONOperations;
import com.redis.spring.ops.pds.BloomOperations;
import com.redis.spring.ops.search.SearchOperations;

import io.redisearch.FieldName;
import io.redisearch.Schema;
import io.redisearch.Schema.Field;
import io.redisearch.Schema.FieldType;
import io.redisearch.Schema.TextField;
import io.redisearch.client.Client;
import io.redisearch.client.IndexDefinition;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RedisProperties.class)
@EnableAspectJAutoProxy
@ComponentScan("com.redis.spring.bloom")
public class RedisModulesConfiguration extends CachingConfigurerSupport {

  @Bean(name = "redisModulesClient")
  RedisModulesClient redisModulesClient(JedisConnectionFactory jedisConnectionFactory) {
    return new RedisModulesClient(jedisConnectionFactory);
  }

  @Bean(name = "redisModulesOperations")
  RedisModulesOperations<?, ?> redisModulesOperations(RedisModulesClient rmc) {
    return new RedisModulesOperations<>(rmc);
  }

  @Bean(name = "redisJSONOperations")
  JSONOperations<?> redisJSONOperations(RedisModulesOperations<?, ?> redisModulesOperations) {
    return redisModulesOperations.opsForJSON();
  }

  @Bean(name = "redisBloomOperations")
  BloomOperations<?> redisBloomOperations(RedisModulesOperations<?, ?> redisModulesOperations) {
    return redisModulesOperations.opsForBloom();
  }

  @Bean(name = "redisTemplate")
  @Primary
  public RedisTemplate<?, ?> redisTemplate(JedisConnectionFactory connectionFactory) {
    RedisTemplate<?, ?> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    return template;
  }

  @Bean(name = "redisJSONKeyValueAdapter")
  RedisJSONKeyValueAdapter getRedisJSONKeyValueAdapter(RedisOperations<?, ?> redisOps,
      JSONOperations<?> redisJSONOperations) {
    return new RedisJSONKeyValueAdapter(redisOps, redisJSONOperations);
  }

  @Bean(name = "redisJSONKeyValueTemplate")
  public CustomRedisKeyValueTemplate getRedisJSONKeyValueTemplate(RedisOperations<?, ?> redisOps,
      JSONOperations<?> redisJSONOperations) {
    RedisMappingContext mappingContext = new RedisMappingContext();
    return new CustomRedisKeyValueTemplate(getRedisJSONKeyValueAdapter(redisOps, redisJSONOperations), mappingContext);
  }

  @Bean(name = "redisCustomKeyValueTemplate")
  public CustomRedisKeyValueTemplate getKeyValueTemplate(RedisOperations<?, ?> redisOps,
      JSONOperations<?> redisJSONOperations) {
    RedisMappingContext mappingContext = new RedisMappingContext();
    return new CustomRedisKeyValueTemplate(getRedisJSONKeyValueAdapter(redisOps, redisJSONOperations), mappingContext);
  }

  @EventListener(ContextRefreshedEvent.class)
  public void ensureIndexesAreCreated(ContextRefreshedEvent cre) {
    System.out.println(">>>> On ContextRefreshedEvent ... Creating Indexes......");

    ApplicationContext ac = cre.getApplicationContext();
    @SuppressWarnings("unchecked")
    RedisModulesOperations<String, String> rmo = (RedisModulesOperations<String, String>) ac.getBean("redisModulesOperations");

    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
    provider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
    for (BeanDefinition beanDef : provider
        .findCandidateComponents("com.redis.spring.annotations.document.fixtures")) {
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        System.out.printf(">>>> Found @Document annotated class: %s\n", cl.getSimpleName());

        List<Field> fields = new ArrayList<Field>();
        for (java.lang.reflect.Field field : cl.getDeclaredFields()) {
          System.out.println(">>>> Inspecting field " + field.getName());
          // Text
          if (field.isAnnotationPresent(TextIndexed.class)) {
            System.out.println(">>>>>> FOUND TextIndexed on " + field.getName());
            TextIndexed ti = field.getAnnotation(TextIndexed.class);

            FieldName fieldName = FieldName.of("$." + field.getName());
            if (!ObjectUtils.isEmpty(ti.alias())) {
              fieldName = fieldName.as(ti.alias());
            }
            String phonetic = ObjectUtils.isEmpty(ti.phonetic()) ? null : ti.phonetic();
            TextField tf = new TextField(fieldName, ti.weight(), ti.sortable(), ti.nostem(), ti.noindex(), phonetic);

            fields.add(tf);
          }
          // Tag
          if (field.isAnnotationPresent(TagIndexed.class)) {
            System.out.println(">>>>>> FOUND TagIndexed on " + field.getName());
            TagIndexed ti = field.getAnnotation(TagIndexed.class);

            FieldName fieldName = FieldName.of("$." + field.getName() + "[*]");
            if (!ObjectUtils.isEmpty(ti.alias())) {
              fieldName = fieldName.as(ti.alias());
            }
            Field tf = new Field(fieldName, FieldType.Tag, ti.sortable(), ti.noindex());

            fields.add(tf);
          }
        }

        if (!fields.isEmpty()) {
          Schema schema = new Schema();
          SearchOperations<String> opsForSearch = rmo.opsForSearch(cl.getSimpleName() + "Idx");
          for (Field field : fields) {
            schema.addField(field);
          }
          IndexDefinition def = new IndexDefinition(IndexDefinition.Type.JSON);
          opsForSearch.createIndex(schema, Client.IndexOptions.defaultOptions().setDefinition(def));
        }
      } catch (Exception e) {
        System.err.println(String.format("In ensureIndexesAreCreated: Exception: %s ==> %s", e.getClass().getName(), e.getMessage()));
        e.printStackTrace();
      }
    }

  }

  @EventListener(ContextRefreshedEvent.class)
  public void processBloom(ContextRefreshedEvent cre) {
    System.out.println(">>>> On ContextRefreshedEvent ... Processing Bloom annotations......");
    ApplicationContext ac = cre.getApplicationContext();
    @SuppressWarnings("unchecked")
    RedisModulesOperations<String, String> rmo = (RedisModulesOperations<String, String>) ac.getBean("redisModulesOperations");

    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
    provider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
    provider.addIncludeFilter(new AnnotationTypeFilter(RedisHash.class));
    for (BeanDefinition beanDef : provider
        .findCandidateComponents("com.redis.spring.annotations.bloom.fixtures")) {
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        System.out.printf(">>>> Found @RedisHash / @Document annotated class: %s\n", cl.getSimpleName());

        for (java.lang.reflect.Field field : cl.getDeclaredFields()) {
          System.out.println(">>>> Inspecting field " + field.getName());
          // Text
          if (field.isAnnotationPresent(Bloom.class)) {
            System.out.println(">>>>>> FOUND Bloom on " + field.getName());
            Bloom bloom = field.getAnnotation(Bloom.class);
            BloomOperations<String> ops = rmo.opsForBloom();
            String filterName = !ObjectUtils.isEmpty(bloom.name()) ? bloom.name() : String.format("bf:%s:%s", cl.getSimpleName(), field.getName());
            ops.createFilter(filterName, bloom.capacity(), bloom.errorRate());
          }
        }
      } catch (Exception e) {
        System.err.println("In processBloom: Exception: " + e.getMessage());
      }
    }
  }

}
```

### Query By Example

```
Person person = new Person();
person.setFirstname("Dave");

Example<Person> example = Example.of(person);

MongoRepository repo = …
List<Person> result = repo.findAll(example); /
```

### Finders

Derived queries with the predicates:

* IsStartingWith
* StartingWith
* StartsWith
* IsEndingWith
* EndingWith
* EndsWith
* IsNotContaining
* NotContaining
* NotContains
* IsContaining
* Containing
* Contains

Predicate | Example                           | SQL
---------|------------------------------------|-----------------------------------
Distinct | findDistinctByLastnameAndFirstname | select distinct …​ where x.lastname = ?1 and x.firstname = ?2
And | findByLastnameAndFirstname | … where x.lastname = ?1 and x.firstname = ?2
Or | findByLastnameOrFirstname | … where x.lastname = ?1 or x.firstname = ?2
Is, Equals | findByFirstname,findByFirstnameIs,findByFirstnameEquals | … where x.firstname = ?1
Between | findByStartDateBetween | … where x.startDate between ?1 and ?2
LessThan | findByAgeLessThan | … where x.age < ?1
LessThanEqual | findByAgeLessThanEqual | … where x.age <= ?1
GreaterThan | findByAgeGreaterThan | … where x.age > ?1
GreaterThanEqual | findByAgeGreaterThanEqual | … where x.age >= ?1
After | findByStartDateAfter | … where x.startDate > ?1
Before | findByStartDateBefore | … where x.startDate < ?1
IsNull, Null | findByAge(Is)Null | … where x.age is null
IsNotNull, NotNull | findByAge(Is)NotNull | … where x.age not null
Like | findByFirstnameLike | … where x.firstname like ?1
NotLike | findByFirstnameNotLike | … where x.firstname not like ?1
StartingWith | findByFirstnameStartingWith | … where x.firstname like ?1 (parameter bound with appended %)
EndingWith | findByFirstnameEndingWith | … where x.firstname like ?1 (parameter bound with prepended %)
Containing | findByFirstnameContaining | … where x.firstname like ?1 (parameter bound wrapped in %)
OrderBy | findByAgeOrderByLastnameDesc | … where x.age = ?1 order by x.lastname desc
Not | findByLastnameNot | … where x.lastname <> ?1
In | findByAgeIn(Collection<Age> ages) | … where x.age in ?1
NotIn | findByAgeNotIn(Collection<Age> ages) | … where x.age not in ?1
True | findByActiveTrue() | … where x.active = true
False | findByActiveFalse() | … where x.active = false
IgnoreCase | findByFirstnameIgnoreCase | … where UPPER(x.firstname) = UPPER(?1)





