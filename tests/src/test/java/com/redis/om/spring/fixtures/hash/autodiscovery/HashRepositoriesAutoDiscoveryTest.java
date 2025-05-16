package com.redis.om.spring.fixtures.hash.autodiscovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import com.redis.om.spring.AbstractBaseOMTest;
import com.redis.om.spring.TestConfig;
import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;

@SpringBootTest(
    //
    classes = HashRepositoriesAutoDiscoveryTest.Config.class, //
    properties = { "spring.main.allow-bean-definition-overriding=true" } //
)
@TestPropertySource(
    properties = { "spring.config.location=classpath:vss_on.yaml" }
)
class HashRepositoriesAutoDiscoveryTest extends AbstractBaseOMTest {
  @Autowired
  AHashRepository repository;

  @Test
  void testBasePackageClassesAreFound() {
    AHash aHash = repository.save(AHash.of("A Doc"));

    assertEquals(1, repository.count());

    Optional<AHash> maybeDoc = repository.findById(aHash.getId());

    assertTrue(maybeDoc.isPresent());

    // delete given an id
    repository.deleteById(aHash.getId());

    assertEquals(0, repository.count());
  }

  @SpringBootApplication
  @Configuration
  @EnableRedisEnhancedRepositories()
  static class Config extends TestConfig {
  }

}