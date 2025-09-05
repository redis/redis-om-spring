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
import java.util.Collection;
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
    Optional<DocumentWithMixedTypes> entity = repository.findById(testEntity.getId());
    
    assertTrue(entity.isPresent(), "Entity should be found by id");
    assertEquals("John Doe", entity.get().getName());
    assertEquals(30, entity.get().getAge());
    assertEquals(75000.50, entity.get().getSalary());
    assertTrue(entity.get().getActive());
    assertEquals(LocalDate.of(1993, 5, 15), entity.get().getBirthDate());
  }

  @Test
  void testProjectionWithoutValueAnnotation_AllFieldsShouldWork() {
    // After the fix, non-String fields should work without @Value annotation
    Optional<DocumentMixedTypesProjection> projection = repository.findByName("John Doe");
    
    assertTrue(projection.isPresent(), "Projection should be present");
    
    // All fields should now work without @Value annotation
    assertEquals("John Doe", projection.get().getName(), "String field should work");
    assertEquals(30, projection.get().getAge(), "Integer field should work WITHOUT @Value annotation");
    assertEquals(75000.50, projection.get().getSalary(), "Double field should work WITHOUT @Value annotation");
    assertTrue(projection.get().getActive(), "Boolean field should work WITHOUT @Value annotation");
    assertEquals(LocalDate.of(1993, 5, 15), projection.get().getBirthDate(), 
        "LocalDate field should work WITHOUT @Value annotation");
  }

  @Test
  void testProjectionWithValueAnnotation_AllFieldsWork() {
    // Test that all fields work correctly with @Value annotation (the workaround)
    Collection<DocumentMixedTypesProjectionFixed> projections = repository.findAllByName("John Doe");
    
    assertFalse(projections.isEmpty(), "Projections should be present");
    DocumentMixedTypesProjectionFixed projection = projections.iterator().next();
    
    // All fields should work with @Value annotation
    assertEquals("John Doe", projection.getName(), "String field should work");
    assertEquals(30, projection.getAge(), "Integer field should work with @Value");
    assertEquals(75000.50, projection.getSalary(), "Double field should work with @Value");
    assertTrue(projection.getActive(), "Boolean field should work with @Value");
    assertEquals(LocalDate.of(1993, 5, 15), projection.getBirthDate(), 
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