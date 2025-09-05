package com.redis.om.spring.repository;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.DocumentWithMixedTypes;
import com.redis.om.spring.fixtures.document.repository.DocumentMixedTypesProjection;
import com.redis.om.spring.fixtures.document.repository.DocumentMixedTypesProjectionFixed;
import com.redis.om.spring.fixtures.document.repository.DocumentMixedTypesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to reproduce and demonstrate issue #650:
 * Projection interfaces return null for non-String fields when not using @Value annotation
 */
class DocumentProjectionMixedTypesTest extends AbstractBaseDocumentTest {

  @Autowired
  private DocumentMixedTypesRepository repository;

  private DocumentWithMixedTypes testEntity;

  @BeforeEach
  void setUp() {
    testEntity = DocumentWithMixedTypes.builder()
        .name("John Doe")
        .age(30)
        .salary(75000.50)
        .active(true)
        .birthDate(LocalDate.of(1993, 5, 15))
        .description("Test employee")
        .build();
    
    testEntity = repository.save(testEntity);
  }

  @Test
  void testEntityFetch_VerifyDataExists() {
    // First verify the entity exists with proper data
    Optional<DocumentWithMixedTypes> entity = repository.findByName("John Doe");
    
    assertTrue(entity.isPresent(), "Entity should be found by name");
    assertEquals("John Doe", entity.get().getName());
    assertEquals(30, entity.get().getAge());
    assertEquals(75000.50, entity.get().getSalary());
    assertTrue(entity.get().getActive());
    assertEquals(LocalDate.of(1993, 5, 15), entity.get().getBirthDate());
  }

  @Test
  void testProjectionWithoutValueAnnotation_NonStringFieldsReturnNull() {
    // This test demonstrates the issue - non-String fields return null without @Value
    Optional<DocumentMixedTypesProjection> projection = repository.findFirstByName("John Doe");
    
    assertTrue(projection.isPresent(), "Projection should be present");
    
    // String field should work
    assertEquals("John Doe", projection.get().getName(), "String field should work without @Value");
    
    // These assertions demonstrate the issue - all non-String fields return null
    assertNull(projection.get().getAge(), 
        "Integer field returns null without @Value annotation - this is the issue!");
    assertNull(projection.get().getSalary(), 
        "Double field returns null without @Value annotation - this is the issue!");
    assertNull(projection.get().getActive(), 
        "Boolean field returns null without @Value annotation - this is the issue!");
    assertNull(projection.get().getBirthDate(), 
        "LocalDate field returns null without @Value annotation - this is the issue!");
  }

  @Test
  void testProjectionWithValueAnnotation_AllFieldsWork() {
    // Test that all fields work correctly with @Value annotation (the workaround)
    Optional<DocumentMixedTypesProjectionFixed> projection = repository.findOneByName("John Doe");
    
    assertTrue(projection.isPresent(), "Projection should be present");
    
    // All fields should work with @Value annotation
    assertEquals("John Doe", projection.get().getName(), "String field should work");
    assertEquals(30, projection.get().getAge(), "Integer field should work with @Value");
    assertEquals(75000.50, projection.get().getSalary(), "Double field should work with @Value");
    assertTrue(projection.get().getActive(), "Boolean field should work with @Value");
    assertEquals(LocalDate.of(1993, 5, 15), projection.get().getBirthDate(), 
        "LocalDate field should work with @Value");
  }

  @Test
  void testDirectEntityFetch_AllFieldsWork() {
    // Verify that the entity itself has all fields correctly stored
    Optional<DocumentWithMixedTypes> entity = repository.findById(testEntity.getId());
    
    assertTrue(entity.isPresent(), "Entity should be present");
    assertEquals("John Doe", entity.get().getName());
    assertEquals(30, entity.get().getAge());
    assertEquals(75000.50, entity.get().getSalary());
    assertTrue(entity.get().getActive());
    assertEquals(LocalDate.of(1993, 5, 15), entity.get().getBirthDate());
  }

  @AfterEach
  void tearDown() {
    repository.deleteAll();
  }
}