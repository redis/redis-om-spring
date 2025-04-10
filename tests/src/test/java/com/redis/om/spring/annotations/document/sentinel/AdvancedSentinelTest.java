package com.redis.om.spring.annotations.document.sentinel;

import com.redis.om.spring.AbstractBaseDocumentSentinelTest;
import com.redis.om.spring.fixtures.document.model.Company;
import com.redis.om.spring.fixtures.document.model.CompanyMeta;
import com.redis.om.spring.fixtures.document.model.Employee;
import com.redis.om.spring.fixtures.document.repository.CompanyRepository;
import com.redis.om.spring.search.stream.SearchStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test demonstrates more advanced functionality of Redis OM Spring
 * with a Redis Sentinel configuration. It tests various operations including
 * searches, filters, and the bloom filter functionality.
 */
@DisabledIfEnvironmentVariable(named = "GITHUB_ACTIONS", matches = "true")
class AdvancedSentinelTest extends AbstractBaseDocumentSentinelTest {
  @Autowired
  CompanyRepository repository;
  
  @Autowired
  RedisConnectionFactory connectionFactory;
  
  private Company redis;
  private Company microsoft;
  private Company apple;
  
  @BeforeEach
  void setUp() {
    // Create test data
    redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), 
        new Point(-122.066540, 37.377690), "stack@redis.com");
    redis.setTags(Set.of("database", "nosql", "redis"));
    redis.setMetaList(Set.of(CompanyMeta.of("Redis", 100, Set.of("RedisTag"))));
    redis.setPubliclyListed(false);
    redis.setEmployees(Set.of(
        Employee.of("John Doe"),
        Employee.of("Jane Smith")
    ));
    
    microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), 
        new Point(-122.124500, 47.640160), "research@microsoft.com");
    microsoft.setTags(Set.of("software", "cloud", "windows"));
    microsoft.setMetaList(Set.of(CompanyMeta.of("MS", 50, Set.of("MsTag"))));
    microsoft.setPubliclyListed(true);
    
    apple = Company.of("Apple", 1976, LocalDate.of(2022, 9, 10), 
        new Point(-122.0322, 37.3220), "info@apple.com");
    apple.setTags(Set.of("hardware", "software", "mobile"));
    apple.setMetaList(Set.of(CompanyMeta.of("AAPL", 75, Set.of("AppleTag"))));
    apple.setPubliclyListed(true);
    
    repository.saveAll(List.of(redis, microsoft, apple));
  }
  
  @AfterEach
  void tearDown() {
    repository.deleteAll();
  }
  
  @Test
  void testConnectionFactory() {
    assertThat(connectionFactory).isNotNull();
    assertThat(connectionFactory.getConnection()).isNotNull();
    assertThat(connectionFactory.getConnection().ping()).isEqualTo("PONG");
  }
  
  @Test
  void testBasicQueries() {
    // Count
    assertEquals(3, repository.count());
    
    // Find by ID
    Optional<Company> maybeRedis = repository.findById(redis.getId());
    assertTrue(maybeRedis.isPresent());
    assertEquals(redis, maybeRedis.get());
    
    // Find by property
    List<Company> redisCompanies = repository.findByName("RedisInc");
    assertEquals(1, redisCompanies.size());
    assertEquals(redis, redisCompanies.get(0));
  }
  
  @Test
  void testBloomFilter() {
    // Test Bloom filter annotations
    assertTrue(repository.existsByEmail("stack@redis.com"));
    assertTrue(repository.existsByEmail("research@microsoft.com"));
    assertTrue(repository.existsByEmail("info@apple.com"));
  }
  
  @Test
  void testGeoSpatialQueries() {
    // Test geo-spatial queries
    Point siliconValley = new Point(-122.1, 37.4);
    Distance tenMiles = new Distance(10, Metrics.MILES);
    
    Iterable<Company> nearbyCompanies = repository.findByLocationNear(siliconValley, tenMiles);
    List<Company> nearbyList = (List<Company>) nearbyCompanies;
    
    assertEquals(2, nearbyList.size());
    assertTrue(nearbyList.contains(redis));
    assertTrue(nearbyList.contains(apple));
  }
  
  @Test
  void testSearchStream() {
    // Count all companies
    assertEquals(3, repository.count());
    
    // Test filter for publicly listed companies
    List<Company> publicCompanies = repository.findByPubliclyListed(true);
    assertEquals(2, publicCompanies.size());
    assertTrue(publicCompanies.contains(microsoft));
    assertTrue(publicCompanies.contains(apple));
    
    // Test complex query with regular Stream API
    List<Company> filteredCompanies = repository.findAll()
        .stream()
        .filter(c -> c.getYearFounded() > 1975)
        .filter(c -> c.getTags().contains("redis"))
        .collect(java.util.stream.Collectors.toList());
    
    assertEquals(1, filteredCompanies.size());
    assertEquals(redis, filteredCompanies.get(0));
  }
  
  @Test
  void testNestedQueries() {
    // Test nested property queries
    List<Company> redisMetaCompanies = repository.findByMetaList_stringValue("Redis");
    assertEquals(1, redisMetaCompanies.size());
    assertEquals(redis, redisMetaCompanies.get(0));
    
    // Test employee nested properties
    List<Company> johnCompanies = repository.findByEmployees_name("John Doe");
    assertEquals(1, johnCompanies.size());
    assertEquals(redis, johnCompanies.get(0));
  }
  
  @Test
  void testOrderBy() {
    // Test ordered queries
    List<Company> companiesFoundedAfter1970InAscOrder = repository.findByYearFoundedGreaterThanOrderByNameAsc(1970);
    assertEquals(3, companiesFoundedAfter1970InAscOrder.size());
    assertEquals("Apple", companiesFoundedAfter1970InAscOrder.get(0).getName());
    assertEquals("Microsoft", companiesFoundedAfter1970InAscOrder.get(1).getName());
    assertEquals("RedisInc", companiesFoundedAfter1970InAscOrder.get(2).getName());
    
    List<Company> companiesFoundedAfter1970InDescOrder = repository.findByYearFoundedGreaterThanOrderByNameDesc(1970);
    assertEquals(3, companiesFoundedAfter1970InDescOrder.size());
    assertEquals("RedisInc", companiesFoundedAfter1970InDescOrder.get(0).getName());
    assertEquals("Microsoft", companiesFoundedAfter1970InDescOrder.get(1).getName());
    assertEquals("Apple", companiesFoundedAfter1970InDescOrder.get(2).getName());
  }
}