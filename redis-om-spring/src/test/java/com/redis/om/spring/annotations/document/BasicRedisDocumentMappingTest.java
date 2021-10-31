package com.redis.om.spring.annotations.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.Company;
import com.redis.om.spring.annotations.document.fixtures.CompanyRepository;

public class BasicRedisDocumentMappingTest extends AbstractBaseDocumentTest {
  @Autowired
  CompanyRepository repository;

  @Test
  public void testBasicCrudOperations() {
    Company redis = repository.save(Company.of("RedisInc"));
    Company microsoft = repository.save(Company.of("Microsoft"));

    assertEquals(2, repository.count());

    Optional<Company> maybeRedisLabs = repository.findById(redis.getId());
    Optional<Company> maybeMicrosoft = repository.findById(microsoft.getId());

    assertTrue(maybeRedisLabs.isPresent());
    assertTrue(maybeMicrosoft.isPresent());

    assertEquals(redis, maybeRedisLabs.get());
    assertEquals(microsoft, maybeMicrosoft.get());
  }
}
