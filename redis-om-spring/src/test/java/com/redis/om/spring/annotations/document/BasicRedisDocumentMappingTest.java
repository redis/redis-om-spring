package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.Company;
import com.redis.om.spring.annotations.document.fixtures.Company$;
import com.redis.om.spring.annotations.document.fixtures.CompanyRepository;

public class BasicRedisDocumentMappingTest extends AbstractBaseDocumentTest {
  @Autowired
  CompanyRepository repository;
  
  @BeforeEach
  public void cleanUp() {
    repository.deleteAll();
  }

  @Test
  public void testBasicCrudOperations() {
    Company redis = repository.save(Company.of("RedisInc", 2011, new Point(-122.066540, 37.377690), "stack@redis.com"));
    Company microsoft = repository.save(Company.of("Microsoft", 1975, new Point(-122.124500, 47.640160), "research@microsoft.com"));

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
  
  @Test
  public void testUpdateSingleField() {
    Company redisInc = repository.save(Company.of("RedisInc", 2011, new Point(-122.066540, 37.377690), "stack@redis.com"));
    repository.updateField(redisInc, Company$.NAME, "Redis");
    
    Optional<Company> maybeRedis = repository.findById(redisInc.getId());

    assertTrue(maybeRedis.isPresent());

    assertEquals("Redis", maybeRedis.get().getName());
  }
  
  @Test
  public void testAuditAnnotations() {
    Company redis = repository.save(Company.of("RedisInc", 2011, new Point(-122.066540, 37.377690), "stack@redis.com"));
    Company microsoft = repository.save(Company.of("Microsoft", 1975, new Point(-122.124500, 47.640160), "research@microsoft.com"));
    
    // created dates should not be null
    assertNotNull(redis.getCreatedDate());
    assertNotNull(microsoft.getCreatedDate());
    
    // created dates should be null upon creation
    assertNull(redis.getLastModifiedDate());
    assertNull(microsoft.getLastModifiedDate());
    
    repository.save(redis);
    repository.save(microsoft);
    
    // last modified dates should not be null after a second save
    assertNotNull(redis.getLastModifiedDate());
    assertNotNull(microsoft.getLastModifiedDate());
  }
  
  @Test
  public void testGetFieldsByIds() {
    Company redis = repository.save(Company.of("RedisInc", 2011, new Point(-122.066540, 37.377690), "stack@redis.com"));
    Company microsoft = repository.save(Company.of("Microsoft", 1975, new Point(-122.124500, 47.640160), "research@microsoft.com"));

    Iterable<String> ids = List.of(redis.getId(), microsoft.getId());
    Iterable<String> companyNames = repository.getFieldsByIds(ids, Company$.NAME);
    assertThat(companyNames).containsExactly(redis.getName(), microsoft.getName());
  }
  
  @Test
  public void testDynamicBloomRepositoryMethod() {
    Company redis = repository.save(Company.of("RedisInc", 2011, new Point(-122.066540, 37.377690), "stack@redis.com"));
    Company microsoft = repository.save(Company.of("Microsoft", 1975, new Point(-122.124500, 47.640160), "research@microsoft.com"));

    assertTrue(repository.existsByEmail(redis.getEmail()));
    assertTrue(repository.existsByEmail(microsoft.getEmail()));
    assertFalse(repository.existsByEmail("bsb@redis.com"));
  }
  
}
