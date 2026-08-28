package com.redis.om.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.documents.domain.Person;
import com.redis.om.documents.repositories.PersonRepository;

/**
 * Reproduces https://github.com/redis/redis-om-spring/issues/769 end to end,
 * with no mocking: this module pins {@code spring-data-redis} to 4.1.0 (see
 * {@code build.gradle}), matching the reporter's exact environment (Spring
 * Boot 4.1.0 / Spring Data Redis 4.1.0 / Jedis 7.4.1, no Lettuce on the
 * classpath, default Spring Boot auto-configured pooled
 * {@code JedisConnectionFactory}). Spring Data Redis 4.1 defaults
 * {@code JedisConnectionFactory.useUnifiedJedis} to {@code true} whenever
 * {@code redis.clients.jedis.RedisClient} is present (Jedis 7.2+), so
 * {@code getConnection().getNativeConnection()} returns a
 * {@code UnifiedJedis}-based connection instead of a raw {@code Jedis},
 * which breaks {@code RedisModulesClient#getJedis()}.
 * <p>
 * {@code Person} has no {@code @Lexicographic} fields, so this hits the same
 * asymmetry the reporter described: {@code save()} takes the safe fast path
 * that never calls {@code getJedis()}, while {@code saveAll()} always
 * pipelines through it and throws {@code NoSuchElementException: No value
 * present}.
 */
class Issue769SaveAllTest extends AbstractDocumentTest {

  @Autowired
  PersonRepository personRepository;

  @AfterEach
  void cleanUp() {
    personRepository.deleteAll();
  }

  @Test
  void testSaveAllWithRealJedisConnectionFactory() {
    Person person = Person.of("Jane", "Doe", "jane.doe@example.com");

    List<Person> saved = personRepository.saveAll(List.of(person));

    assertThat(saved).hasSize(1);
    assertThat(personRepository.findById(saved.get(0).getId())).isPresent();
  }
}
