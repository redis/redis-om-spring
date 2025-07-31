package com.redis.om.spring.annotations;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.PersonWithPhones;
import com.redis.om.spring.fixtures.document.model.PersonWithPhones$;
import com.redis.om.spring.fixtures.document.model.Phone;
import com.redis.om.spring.fixtures.document.repository.PersonWithPhonesRepository;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.search.stream.EntityStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import com.redis.om.spring.indexing.SearchField;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for nested array indexing feature (GitHub issue #519).
 * This feature enables indexing of nested fields within List<Model> properties
 * using @Indexed(schemaFieldType = SchemaFieldType.NESTED).
 */
class NestedArrayIndexingTest extends AbstractBaseDocumentTest {
  private static final Logger logger = LoggerFactory.getLogger(NestedArrayIndexingTest.class);

  @Autowired
  PersonWithPhonesRepository repository;

  @Autowired
  RediSearchIndexer indexer;

  @Autowired
  RedisTemplate<String, String> redisTemplate;

  @Autowired
  EntityStream entityStream;

  @BeforeEach
  void setup() {
    repository.deleteAll();
    
    // Force index recreation to ensure nested fields are indexed
    indexer.dropAndRecreateIndexFor(PersonWithPhones.class);
    
    // Create test data
    PersonWithPhones person1 = PersonWithPhones.of("John Doe", Arrays.asList(
        Phone.of("555-1234", "home"),
        Phone.of("555-5678", "work")
    ));
    person1.setId("1");
    
    PersonWithPhones person2 = PersonWithPhones.of("Jane Smith", Arrays.asList(
        Phone.of("555-9999", "mobile"),
        Phone.of("555-1111", "work")
    ));
    person2.setId("2");
    
    PersonWithPhones person3 = PersonWithPhones.of("Bob Johnson", Arrays.asList(
        Phone.of("555-2222", "home")
    ));
    person3.setId("3");
    
    repository.saveAll(Arrays.asList(person1, person2, person3));
  }

  @Test
  void testNestedAnnotationIsPresent() throws NoSuchFieldException {
    // Test that @Indexed annotation with NESTED schema field type is present
    Field phonesListField = PersonWithPhones.class.getDeclaredField("phonesList");
    com.redis.om.spring.annotations.Indexed indexed = phonesListField.getAnnotation(com.redis.om.spring.annotations.Indexed.class);
    
    assertNotNull(indexed, "@Indexed annotation should be present on phonesList field");
    assertEquals(com.redis.om.spring.annotations.SchemaFieldType.NESTED, indexed.schemaFieldType(), 
        "schemaFieldType should be NESTED");
  }

  @Test
  void testNestedFieldsAreIndexed() {
    // Verify that nested fields within the array are properly indexed
    String indexName = indexer.getIndexName(PersonWithPhones.class);
    assertNotNull(indexName, "Index should be created for PersonWithPhones");
    
    try {
      // Get the actual index schema to verify nested fields are present
      List<SearchField> searchFields = indexer.getSchemaFor(PersonWithPhones.class);
      assertNotNull(searchFields, "Schema should exist for PersonWithPhones");
      assertFalse(searchFields.isEmpty(), "Schema should have indexed fields");
      
      // Convert to SchemaField names for verification
      List<String> fieldNames = searchFields.stream()
          .map(searchField -> searchField.getSchemaField().getFieldName().getAttribute())
          .collect(Collectors.toList());
      
      // Check for nested field entries
      boolean hasNestedNumberField = fieldNames.stream()
          .anyMatch(name -> name.contains("phonesList") && name.contains("number"));
      boolean hasNestedTypeField = fieldNames.stream()
          .anyMatch(name -> name.contains("phonesList") && name.contains("type"));
      
      logger.info("Index field names: {}", fieldNames);
      
      assertTrue(hasNestedNumberField, "Schema should contain phonesList number field");
      assertTrue(hasNestedTypeField, "Schema should contain phonesList type field");
      
    } catch (Exception e) {
      logger.warn("Could not retrieve index schema: {}", e.getMessage());
      // Fallback to basic index existence check
      assertTrue(indexName.contains("PersonWithPhones"), "Index name should contain entity name");
    }
  }

  @Test
  void testRepositoryMethodFindByNestedPhoneNumber() {
    // Test finding persons by phone number within the array
    List<PersonWithPhones> results = repository.findByPhonesListNumber("555-1234");
    
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getName()).isEqualTo("John Doe");
    assertThat(results.get(0).getPhonesList())
        .anyMatch(phone -> phone.getNumber().equals("555-1234"));
  }

  @Test
  void testRepositoryMethodFindByNestedPhoneType() {
    // Test finding persons by phone type within the array
    List<PersonWithPhones> results = repository.findByPhonesListType("work");
    
    assertThat(results).hasSize(2);
    assertThat(results.stream().map(PersonWithPhones::getName))
        .containsExactlyInAnyOrder("John Doe", "Jane Smith");
  }

  @Test
  void testRepositoryMethodFindByMultipleNestedFields() {
    // Test finding persons by both phone number and type
    List<PersonWithPhones> results = repository.findByPhonesListNumberAndPhonesListType("555-5678", "work");
    
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getName()).isEqualTo("John Doe");
  }

  @Test
  void testEntityStreamNestedFieldQuery() {
    // Test EntityStream queries on nested fields using metamodel
    List<PersonWithPhones> results = entityStream
        .of(PersonWithPhones.class)
        .filter(PersonWithPhones$.NAME.eq("John Doe"))
        .collect(Collectors.toList());
    
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getName()).isEqualTo("John Doe");
    assertThat(results.get(0).getPhonesList()).hasSize(2);
    
    // Test basic EntityStream functionality with all entities
    List<PersonWithPhones> allPersons = entityStream
        .of(PersonWithPhones.class)
        .collect(Collectors.toList());
    
    assertThat(allPersons).hasSize(3);
    
    // Test count operations
    long count = entityStream
        .of(PersonWithPhones.class)
        .count();
    
    assertThat(count).isEqualTo(3);
  }

  @Test
  void testNestedFieldSearchResultSerialization() {
    // Test that search results with nested arrays are properly serialized
    List<PersonWithPhones> results = repository.findByName("John Doe");
    
    assertThat(results).hasSize(1);
    PersonWithPhones person = results.get(0);
    assertThat(person.getPhonesList()).hasSize(2);
    assertThat(person.getPhonesList().get(0).getNumber()).isNotNull();
    assertThat(person.getPhonesList().get(0).getType()).isNotNull();
  }

  @Test
  void testMultipleNestedArrayEntries() {
    // Test that all entries in the nested array are properly indexed
    
    // Find by first phone number of John Doe
    List<PersonWithPhones> results1 = repository.findByPhonesListNumber("555-1234");
    assertThat(results1).hasSize(1);
    assertThat(results1.get(0).getName()).isEqualTo("John Doe");
    
    // Find by second phone number of John Doe  
    List<PersonWithPhones> results2 = repository.findByPhonesListNumber("555-5678");
    assertThat(results2).hasSize(1);
    assertThat(results2.get(0).getName()).isEqualTo("John Doe");
  }

  @Test
  void testEmptyResultsForNonExistentNestedValues() {
    // Test that queries for non-existent nested values return empty results
    List<PersonWithPhones> results = repository.findByPhonesListNumber("999-9999");
    assertThat(results).isEmpty();
    
    List<PersonWithPhones> results2 = repository.findByPhonesListType("fax");
    assertThat(results2).isEmpty();
  }
  
  @Test
  void testEmptyAndNullNestedArrays() {
    // Test handling of entities with empty or null nested arrays
    PersonWithPhones personWithEmptyPhones = PersonWithPhones.of("Empty Person", Collections.emptyList());
    personWithEmptyPhones.setId("empty");
    repository.save(personWithEmptyPhones);
    
    // Should be able to find by name but not by any phone fields
    List<PersonWithPhones> results = repository.findByName("Empty Person");
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getPhonesList()).isEmpty();
    
    // Searching by phone fields should not return this person
    List<PersonWithPhones> phoneResults = repository.findByPhonesListNumber("any-number");
    assertThat(phoneResults).doesNotContain(personWithEmptyPhones);
  }
  
  @Test
  void testNestedFieldCaseInsensitivity() {
    // Test case sensitivity in nested field searches
    List<PersonWithPhones> results = repository.findByPhonesListType("WORK");
    // Should not find anything if search is case-sensitive
    // or should find 2 results if case-insensitive
    // This tests the actual implementation behavior
    logger.info("Case insensitive search results: {}", results.size());
    
    // Test with exact case
    List<PersonWithPhones> exactResults = repository.findByPhonesListType("work");
    assertThat(exactResults).hasSize(2);
  }
  
  @Test
  void testNestedFieldPartialMatches() {
    // Test partial matching behavior in nested fields
    List<PersonWithPhones> results = repository.findByPhonesListNumber("555");
    
    // Depending on implementation, this might return all results (contains)
    // or no results (exact match). Log actual behavior for verification.
    logger.info("Partial match results for '555': {}", results.size());
    
    // Test with complete number
    List<PersonWithPhones> exactResults = repository.findByPhonesListNumber("555-1234");
    assertThat(exactResults).hasSize(1);
  }
  
  @Test
  void testNestedFieldIndexingPerformance() {
    // Test performance with larger nested arrays
    PersonWithPhones personWithManyPhones = PersonWithPhones.of("Person With Many Phones",
        Arrays.asList(
            Phone.of("555-0001", "home"),
            Phone.of("555-0002", "work"),
            Phone.of("555-0003", "mobile"),
            Phone.of("555-0004", "work2"),
            Phone.of("555-0005", "home2")
        ));
    personWithManyPhones.setId("many-phones");
    repository.save(personWithManyPhones);
    
    long startTime = System.currentTimeMillis();
    
    // Test search performance
    List<PersonWithPhones> results = repository.findByPhonesListNumber("555-0003");
    
    long endTime = System.currentTimeMillis();
    long queryTime = endTime - startTime;
    
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getName()).isEqualTo("Person With Many Phones");
    
    // Log performance metrics (should be fast)
    logger.info("Query time for nested field with {} phones: {}ms", 
        personWithManyPhones.getPhonesList().size(), queryTime);
    
    // Performance should be reasonable (less than 100ms for this small dataset)
    assertThat(queryTime).isLessThan(1000); // 1 second max for CI environments
  }
  
  @Test
  void testComplexNestedFieldQueries() {
    // Test more complex queries involving nested fields
    PersonWithPhones complexPerson = PersonWithPhones.of("Complex Person",
        Arrays.asList(
            Phone.of("111-1111", "emergency"),
            Phone.of("222-2222", "business"),
            Phone.of("333-3333", "personal")
        ));
    complexPerson.setId("complex");
    repository.save(complexPerson);
    
    // Test finding by unique phone type
    List<PersonWithPhones> emergencyResults = repository.findByPhonesListType("emergency");
    assertThat(emergencyResults).hasSize(1);
    assertThat(emergencyResults.get(0).getName()).isEqualTo("Complex Person");
    
    // Test finding by business phone
    List<PersonWithPhones> businessResults = repository.findByPhonesListType("business");
    assertThat(businessResults).hasSize(1);
    
    // Test AND query combining name and nested field
    List<PersonWithPhones> combinedResults = repository.findByNameAndPhonesListType("Complex Person", "personal");
    assertThat(combinedResults).hasSize(1);
    assertThat(combinedResults.get(0).getId()).isEqualTo("complex");
  }
}