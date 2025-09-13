package com.redis.om.spring.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.HashWithSearchStream;
import com.redis.om.spring.fixtures.hash.model.HashWithSearchStream$;
import com.redis.om.spring.fixtures.hash.repository.HashWithSearchStreamRepository;
import com.redis.om.spring.search.stream.SearchStream;

/**
 * Test to verify that hash repositories can return SearchStream for fluent query operations.
 * This validates that SearchStream works for both JSON documents and Redis Hash entities.
 */
class SearchStreamHashRepositoryTest extends AbstractBaseEnhancedRedisTest {
  
  @Autowired
  HashWithSearchStreamRepository repository;
  
  private HashWithSearchStream john;
  private HashWithSearchStream jane;
  private HashWithSearchStream bob;
  private HashWithSearchStream alice;
  private HashWithSearchStream charlie;
  
  @BeforeEach
  void setUp() {
    // Create test people with properly indexed fields
    john = HashWithSearchStream.of("John Doe", "john@example.com", "Engineering", 35, true);
    john.setSkills(Set.of("Java", "Spring", "Redis"));
    
    jane = HashWithSearchStream.of("Jane Smith", "jane@example.com", "Marketing", 28, true);
    jane.setSkills(Set.of("SEO", "Content", "Analytics"));
    
    bob = HashWithSearchStream.of("Bob Johnson", "bob@example.com", "Engineering", 42, false);
    bob.setSkills(Set.of("Python", "Docker", "Kubernetes"));
    
    alice = HashWithSearchStream.of("Alice Williams", "alice@example.com", "HR", 31, true);
    alice.setSkills(Set.of("Recruiting", "Training", "Compliance"));
    
    charlie = HashWithSearchStream.of("Charlie Brown", "charlie@example.com", "Engineering", 55, false);
    charlie.setSkills(Set.of("Java", "Architecture", "Microservices"));
    
    repository.saveAll(List.of(john, jane, bob, alice, charlie));
  }
  
  @AfterEach
  void tearDown() {
    repository.deleteAll();
  }
  
  @Test
  void testHashRepositoryReturnsSearchStream() {
    // Test that repository method returns SearchStream for hash entities
    SearchStream<HashWithSearchStream> stream = repository.findByEmail("john@example.com");
    
    assertNotNull(stream, "Repository should return a SearchStream");
    assertThat(stream).isInstanceOf(SearchStream.class);
    
    // Verify the stream contains the expected person
    List<HashWithSearchStream> people = stream.collect(Collectors.toList());
    assertEquals(1, people.size(), "Should find 1 person with john@example.com email");
    assertEquals("John Doe", people.get(0).getName());
  }
  
  @Test
  void testHashSearchStreamFluentOperations() {
    // Test fluent operations on SearchStream returned from repository
    SearchStream<HashWithSearchStream> stream = repository.findByDepartment("Engineering");
    
    // Further filter by active status
    List<HashWithSearchStream> activePeople = stream
        .filter(HashWithSearchStream$.ACTIVE.eq(true))
        .collect(Collectors.toList());
    
    assertEquals(1, activePeople.size(), "Should find 1 active person in Engineering");
    assertEquals("John Doe", activePeople.get(0).getName());
  }
  
  @Test
  void testHashSearchStreamMapOperation() {
    // Test map operation on SearchStream to extract emails
    SearchStream<HashWithSearchStream> stream = repository.findByDepartment("Engineering");
    
    // Map to names
    List<String> names = stream
        .map(HashWithSearchStream$.NAME)
        .collect(Collectors.toList());
    
    assertEquals(3, names.size(), "Should find 3 people in Engineering");
    assertThat(names).containsExactlyInAnyOrder("John Doe", "Bob Johnson", "Charlie Brown");
  }
  
  @Test
  void testHashSearchStreamChainedFilters() {
    // Test multiple chained filter operations
    SearchStream<HashWithSearchStream> stream = repository.findByAgeGreaterThan(30);
    
    // Chain multiple filters - age > 30 and active
    List<HashWithSearchStream> filteredPeople = stream
        .filter(HashWithSearchStream$.ACTIVE.eq(true))
        .collect(Collectors.toList());
    
    assertEquals(2, filteredPeople.size(), "Should find 2 active people over 30");
    
    List<String> names = filteredPeople.stream()
        .map(HashWithSearchStream::getName)
        .sorted()
        .collect(Collectors.toList());
    
    assertThat(names).containsExactly("Alice Williams", "John Doe");
  }
  
  @Test
  void testHashSearchStreamWithSort() {
    // Test sorting capabilities of SearchStream
    SearchStream<HashWithSearchStream> stream = repository.findByDepartment("Engineering");
    
    // Sort by age ascending
    List<HashWithSearchStream> sortedPeople = stream
        .sorted(HashWithSearchStream$.AGE, redis.clients.jedis.search.aggr.SortedField.SortOrder.ASC)
        .collect(Collectors.toList());
    
    assertEquals(3, sortedPeople.size());
    
    // Verify sorting order by age
    assertEquals("John Doe", sortedPeople.get(0).getName());     // 35
    assertEquals("Bob Johnson", sortedPeople.get(1).getName());  // 42
    assertEquals("Charlie Brown", sortedPeople.get(2).getName()); // 55
  }
  
  @Test
  void testHashSearchStreamLimit() {
    // Test limit operation on SearchStream
    SearchStream<HashWithSearchStream> stream = repository.findByDepartment("Engineering");
    
    // Limit to first 2 results
    List<HashWithSearchStream> limitedPeople = stream
        .limit(2)
        .collect(Collectors.toList());
    
    assertEquals(2, limitedPeople.size(), "Should return only 2 people due to limit");
  }
  
  @Test
  void testHashSearchStreamCount() {
    // Test count operation on SearchStream
    SearchStream<HashWithSearchStream> stream = repository.findByDepartment("Engineering");
    
    long count = stream.count();
    
    assertEquals(3, count, "Should count 3 people in Engineering department");
  }
  
  @Test
  void testHashSearchStreamEmptyResult() {
    // Test SearchStream with no matching results
    SearchStream<HashWithSearchStream> stream = repository.findByEmail("nonexistent@example.com");
    
    List<HashWithSearchStream> people = stream.collect(Collectors.toList());
    
    assertTrue(people.isEmpty(), "Should return empty list for nonexistent email");
  }
  
  @Test
  void testHashSearchStreamComplexQuery() {
    // Test a complex query combining multiple operations
    SearchStream<HashWithSearchStream> stream = repository.findByActive(true);
    
    // Complex query: active people, filter by department, map to emails
    List<String> emails = stream
        .filter(HashWithSearchStream$.DEPARTMENT.in("Engineering", "HR"))
        .map(HashWithSearchStream$.EMAIL)
        .collect(Collectors.toList());
    
    assertEquals(2, emails.size(), "Should find 2 active people in Engineering or HR");
    assertThat(emails).containsExactlyInAnyOrder("john@example.com", "alice@example.com");
  }
  
  @Test
  void testHashSearchStreamWithSkills() {
    // Test SearchStream with Set field (skills)
    SearchStream<HashWithSearchStream> stream = repository.findBySkills(Set.of("Java"));
    
    List<HashWithSearchStream> javaDevs = stream.collect(Collectors.toList());
    
    assertEquals(2, javaDevs.size(), "Should find 2 people with Java skill");
    
    List<String> names = javaDevs.stream()
        .map(HashWithSearchStream::getName)
        .sorted()
        .collect(Collectors.toList());
    
    assertThat(names).containsExactly("Charlie Brown", "John Doe");
  }
}