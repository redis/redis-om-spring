package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.ReducerFunction;
import com.redis.om.spring.fixtures.hash.model.Person;
import com.redis.om.spring.fixtures.hash.model.Person$;
import com.redis.om.spring.fixtures.hash.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for projection and maps support in aggregations for hash entities.
 * Ensures the features work correctly for @RedisHash entities.
 */
class AggregationHashProjectionTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  PersonRepository repository;

  @Autowired
  EntityStream entityStream;

  // Projection interfaces
  public interface PersonNameProjection {
    String getName();
  }

  public interface PersonDetailsProjection {
    String getName();
    String getEmail();
  }

  public interface PersonWithIdProjection {
    String getId();
    String getName();
  }

  public interface PersonRoleCountProjection {
    String getRoles();
    Long getRoleCount();
  }

  @BeforeEach
  void setup() {
    repository.deleteAll();

    // Create test data
    Person person1 = Person.of("John Doe", "john.doe@example.com", "johnny");
    person1.setId("person1");
    person1.setRoles(Set.of("admin", "developer"));
    person1.setFavoriteFoods(Set.of("pizza", "pasta"));

    Person person2 = Person.of("Jane Smith", "jane.smith@example.com", "jane");
    person2.setId("person2");
    person2.setRoles(Set.of("user", "tester"));
    person2.setFavoriteFoods(Set.of("sushi", "salad"));

    Person person3 = Person.of("Bob Johnson", "bob.johnson@example.com", "bobby");
    person3.setId("person3");
    person3.setRoles(Set.of("manager"));
    person3.setFavoriteFoods(Set.of("burger", "fries"));

    repository.saveAll(List.of(person1, person2, person3));
  }

  @Test
  void testHashProjectionBasic() {
    // Test basic projection with hash entities
    List<PersonNameProjection> projections = entityStream
        .of(Person.class)
        .load(Person$.NAME)
        .toProjection(PersonNameProjection.class);

    assertThat(projections).hasSize(3);
    
    Set<String> names = projections.stream()
        .map(PersonNameProjection::getName)
        .collect(java.util.stream.Collectors.toSet());
    
    assertThat(names).containsExactlyInAnyOrder("Bob Johnson", "Jane Smith", "John Doe");
  }

  @Test
  void testHashProjectionMultipleFields() {
    // Test projection with multiple fields
    List<PersonDetailsProjection> projections = entityStream
        .of(Person.class)
        .filter(Person$.ROLES.in("admin"))
        .load(Person$.NAME, Person$.EMAIL)
        .toProjection(PersonDetailsProjection.class);

    assertThat(projections).hasSize(1);
    
    PersonDetailsProjection projection = projections.get(0);
    assertThat(projection.getName()).isEqualTo("John Doe");
    assertThat(projection.getEmail()).isEqualTo("john.doe@example.com");
  }

  @Test
  void testHashProjectionWithId() {
    // Test that ID is only included when explicitly requested
    List<PersonWithIdProjection> projections = entityStream
        .of(Person.class)
        .filter(Person$.ROLES.in("user"))
        .load(Person$.ID, Person$.NAME)
        .toProjection(PersonWithIdProjection.class);

    assertThat(projections).hasSize(1);
    
    PersonWithIdProjection projection = projections.get(0);
    assertThat(projection.getId()).isEqualTo("person2");
    assertThat(projection.getName()).isEqualTo("Jane Smith");
  }

  @Test
  void testHashProjectionWithGroupBy() {
    // Test projection with grouping using indexed field
    List<PersonRoleCountProjection> projections = entityStream
        .of(Person.class)
        .groupBy(Person$.ROLES)
        .reduce(ReducerFunction.COUNT).as("roleCount")
        .toProjection(PersonRoleCountProjection.class);

    assertThat(projections).isNotEmpty();
    
    // Each role should appear once with count of 1
    for (PersonRoleCountProjection projection : projections) {
      assertThat(projection.getRoleCount()).isEqualTo(1L);
    }
  }

  @Test
  void testHashMapsWithId() {
    // Test toMaps() with hash entities using aggregation on indexed field - ID included by default
    List<Map<String, Object>> maps = entityStream
        .of(Person.class)
        .groupBy(Person$.ROLES)
        .reduce(ReducerFunction.COUNT).as("count")
        .toMaps();

    assertThat(maps).isNotEmpty();
    
    Map<String, Object> firstMap = maps.get(0);
    assertThat(firstMap).containsKey("roles");
    assertThat(firstMap).containsKey("count");
    assertThat(firstMap.get("roles")).isNotNull();
    assertThat(firstMap.get("count")).isNotNull();
  }

  @Test
  void testHashMapsWithoutId() {
    // Test toMaps(false) - ID excluded using aggregation on indexed field
    List<Map<String, Object>> maps = entityStream
        .of(Person.class)
        .groupBy(Person$.ROLES)
        .reduce(ReducerFunction.COUNT).as("count")
        .toMaps(false);

    assertThat(maps).isNotEmpty();
    
    Map<String, Object> firstMap = maps.get(0);
    assertThat(firstMap).doesNotContainKey("id");
    assertThat(firstMap).containsKey("roles");
    assertThat(firstMap).containsKey("count");
    assertThat(firstMap.get("roles")).isNotNull();
    assertThat(firstMap.get("count")).isNotNull();
  }

  @Test
  void testHashMapsWithComplexFields() {
    // Test maps with complex fields (collections)
    List<Map<String, Object>> maps = entityStream
        .of(Person.class)
        .filter(Person$.ROLES.containsAll("admin", "developer"))
        .load(Person$.NAME, Person$.ROLES)
        .toMaps();

    assertThat(maps).hasSize(1);
    
    Map<String, Object> map = maps.get(0);
    assertThat(map).containsKey("name");
    assertThat(map).containsKey("roles");
    assertThat(map.get("name")).isEqualTo("John Doe");
    
    // Redis may return Set as a comma-separated string
    Object roles = map.get("roles");
    if (roles instanceof String) {
      String rolesStr = (String) roles;
      assertThat(rolesStr).contains("admin", "developer");
    } else {
      @SuppressWarnings("unchecked")
      Set<String> rolesSet = (Set<String>) roles;
      assertThat(rolesSet).containsExactlyInAnyOrder("admin", "developer");
    }
  }

  @Test
  void testHashProjectionFieldMapping() {
    // Test that projections handle various field name formats
    List<PersonNameProjection> projections = entityStream
        .of(Person.class)
        .load(Person$.NAME)
        .toProjection(PersonNameProjection.class);

    assertThat(projections).hasSize(3);
    assertThat(projections.get(0).getName()).isNotNull();
  }

  @Test
  void testHashMapsEmptyResults() {
    // Test behavior with empty results
    List<Map<String, Object>> maps = entityStream
        .of(Person.class)
        .filter(Person$.ROLES.in("nonexistent_role"))
        .load(Person$.NAME)
        .toMaps();

    assertThat(maps).isEmpty();
  }

  @Test
  void testHashProjectionPerformance() {
    // Clear existing data first
    repository.deleteAll();
    
    // Add exactly 20 test entities with unique roles
    for (int i = 0; i < 20; i++) {
      Person person = Person.of("Person " + i, "person" + i + "@example.com", "person" + i);
      person.setId("person_" + i);
      person.setRoles(Set.of("role_" + i)); // Each person gets a unique role
      repository.save(person);
    }

    long startTime = System.currentTimeMillis();
    
    // Use aggregation with indexed field to get exactly 20 results
    List<PersonRoleCountProjection> projections = entityStream
        .of(Person.class)
        .groupBy(Person$.ROLES)
        .reduce(ReducerFunction.COUNT).as("roleCount")
        .toProjection(PersonRoleCountProjection.class);
    
    long projectionTime = System.currentTimeMillis() - startTime;
    
    assertThat(projections).hasSize(20);
    
    // Compare with full entity loading
    startTime = System.currentTimeMillis();
    
    List<Person> fullEntities = entityStream
        .of(Person.class)
        .limit(20)
        .collect(java.util.stream.Collectors.toList());
    
    long fullLoadTime = System.currentTimeMillis() - startTime;
    
    assertThat(fullEntities).hasSize(20);
    
    // Log performance metrics
    System.out.println("Projection load time: " + projectionTime + "ms");
    System.out.println("Full entity load time: " + fullLoadTime + "ms");
  }
}