package com.redis.om.spring.fixtures.document.autodiscovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.geo.Point;

import com.redis.om.spring.AbstractBaseOMTest;
import com.redis.om.spring.TestConfig;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.fixtures.document.model.Company;
import com.redis.om.spring.fixtures.document.repository.CompanyRepository;

@SpringBootTest(
    //
    classes = DocRepositoryBasePackageClassesTest.Config.class, //
    properties = { "spring.main.allow-bean-definition-overriding=true" } //
)
class DocRepositoryBasePackageClassesTest extends AbstractBaseOMTest {
  @Autowired
  CompanyRepository repository;

  @Test
  void testBasePackageClassesAreFound() {
    Company redis = repository.save(Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540,
        37.377690), "stack@redis.com"));

    assertEquals(1, repository.count());

    Optional<Company> maybeRedisLabs = repository.findById(redis.getId());

    assertTrue(maybeRedisLabs.isPresent());

    // delete given an id
    repository.deleteById(redis.getId());

    assertEquals(0, repository.count());
  }

  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(
      basePackageClasses = { Company.class, CompanyRepository.class }
  )
  static class Config extends TestConfig {
  }

}