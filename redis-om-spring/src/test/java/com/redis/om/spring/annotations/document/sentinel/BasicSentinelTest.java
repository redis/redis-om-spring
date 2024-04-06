package com.redis.om.spring.annotations.document.sentinel;

import com.redis.om.spring.AbstractBaseDocumentSentinelTest;
import com.redis.om.spring.annotations.document.fixtures.Company;
import com.redis.om.spring.annotations.document.fixtures.CompanyMeta;
import com.redis.om.spring.annotations.document.fixtures.CompanyRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicSentinelTest extends AbstractBaseDocumentSentinelTest {
  @Autowired
  CompanyRepository repository;

  @Disabled("Potential Regression on Sentinel Support - see GH-227")
  @Test
  @DisabledIfEnvironmentVariable(named = "GITHUB_ACTIONS", matches = "true")
  void testBasicCrudOperations() {
    Company redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
      "stack@redis.com");
    redis.setMetaList(Set.of(CompanyMeta.of("Redis", 100, Set.of("RedisTag"))));

    Company microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
      new Point(-122.124500, 47.640160), "research@microsoft.com");
    microsoft.setMetaList(Set.of(CompanyMeta.of("MS", 50, Set.of("MsTag"))));

    repository.saveAll(List.of(redis, microsoft));

    assertEquals(2, repository.count());

    Optional<Company> maybeRedisLabs = repository.findById(redis.getId());
    Optional<Company> maybeMicrosoft = repository.findById(microsoft.getId());

    assertTrue(maybeRedisLabs.isPresent());
    assertTrue(maybeMicrosoft.isPresent());

    assertEquals(redis, maybeRedisLabs.get());
    assertEquals(microsoft, maybeMicrosoft.get());

    // delete given an entity
    repository.delete(microsoft);

    assertEquals(1, repository.count());

    // delete given an id
    repository.deleteById(redis.getId());

    assertEquals(0, repository.count());
  }
}