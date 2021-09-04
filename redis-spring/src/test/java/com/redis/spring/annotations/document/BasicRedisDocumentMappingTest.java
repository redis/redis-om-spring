package com.redis.spring.annotations.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.spring.AbstractBaseTest;
import com.redis.spring.annotations.document.fixtures.Company;
import com.redis.spring.annotations.document.fixtures.CompanyRepository;

public class BasicRedisDocumentMappingTest extends AbstractBaseTest {
  @Autowired
  CompanyRepository repository;

  @Test
  public void testBasicCrudOperations() {
    Company redislabs = repository.save(Company.of("RedisLabs"));
    Company microsoft = repository.save(Company.of("Microsoft"));

    assertEquals(2, repository.count());

    Optional<Company> maybeRedisLabs = repository.findById(redislabs.getId());
    Optional<Company> maybeMicrosoft = repository.findById(microsoft.getId());

    assertTrue(maybeRedisLabs.isPresent());
    assertTrue(maybeMicrosoft.isPresent());

    assertEquals(redislabs, maybeRedisLabs.get());
    assertEquals(microsoft, maybeMicrosoft.get());
  }
}
