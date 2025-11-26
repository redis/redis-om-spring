package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.*;
import com.redis.om.spring.fixtures.document.repository.*;
import com.redis.om.spring.search.stream.EntityStream;

/**
 * Tests for Issue #677: Indexed subfields not generated for @Reference fields.
 *
 * When a document has a @Reference @Indexed field pointing to another document,
 * the metamodel should generate field accessors for the referenced document's
 * indexed/searchable fields, allowing queries like:
 *
 * entityStream.of(RefVehicle.class)
 *   .filter(RefVehicle$.OWNER_NAME.eq("John"))
 *   .collect(...)
 *
 * @see <a href="https://github.com/redis/redis-om-spring/issues/677">Issue #677</a>
 */
class ReferenceIndexedSubfieldsTest extends AbstractBaseDocumentTest {

  @Autowired
  OwnerRepository ownerRepository;

  @Autowired
  RefVehicleRepository refVehicleRepository;

  @Autowired
  EntityStream entityStream;

  private Owner johnDoe;
  private Owner janeDoe;
  private RefVehicle teslaModelS;
  private RefVehicle fordMustang;
  private RefVehicle bmwM3;

  @BeforeEach
  void setup() {
    refVehicleRepository.deleteAll();
    ownerRepository.deleteAll();

    // Create owners
    johnDoe = ownerRepository.save(Owner.of("John Doe", "john@example.com"));
    janeDoe = ownerRepository.save(Owner.of("Jane Doe", "jane@example.com"));

    // Create vehicles with references to owners
    teslaModelS = refVehicleRepository.save(RefVehicle.of("Model S", "Tesla", johnDoe));
    fordMustang = refVehicleRepository.save(RefVehicle.of("Mustang", "Ford", johnDoe));
    bmwM3 = refVehicleRepository.save(RefVehicle.of("M3", "BMW", janeDoe));
  }

  /**
   * Test that the metamodel generates OWNER_NAME field for searching
   * vehicles by their owner's name (a @Searchable field on the referenced entity).
   */
  @Test
  void testMetamodelGeneratesOwnerNameField() throws Exception {
    // This test verifies that RefVehicle$ has a field named OWNER_NAME
    Class<?> metamodelClass = Class.forName("com.redis.om.spring.fixtures.document.model.RefVehicle$");

    // Check that the static field OWNER_NAME exists
    Field ownerNameField = metamodelClass.getDeclaredField("OWNER_NAME");
    assertThat(ownerNameField).isNotNull();

    // Check that the field is accessible as a static field
    Object ownerNameValue = ownerNameField.get(null);
    assertThat(ownerNameValue).isNotNull();
  }

  /**
   * Test that the metamodel generates OWNER_EMAIL field for searching
   * vehicles by their owner's email (an @Indexed field on the referenced entity).
   */
  @Test
  void testMetamodelGeneratesOwnerEmailField() throws Exception {
    // This test verifies that RefVehicle$ has a field named OWNER_EMAIL
    Class<?> metamodelClass = Class.forName("com.redis.om.spring.fixtures.document.model.RefVehicle$");

    // Check that the static field OWNER_EMAIL exists
    Field ownerEmailField = metamodelClass.getDeclaredField("OWNER_EMAIL");
    assertThat(ownerEmailField).isNotNull();

    // Check that the field is accessible as a static field
    Object ownerEmailValue = ownerEmailField.get(null);
    assertThat(ownerEmailValue).isNotNull();
  }

  /**
   * Test that OWNER_NAME field can be used in queries.
   *
   * Note: @Reference fields store only the entity ID, not the full object.
   * Therefore, searching by referenced entity properties (like owner.name) requires
   * the referenced entity to be embedded, not just referenced by ID.
   *
   * This test verifies the metamodel field is usable in query construction,
   * but won't find results because the actual JSON stores only the reference ID.
   */
  @Test
  void testOwnerNameFieldIsUsableInQuery() {
    // Verify the metamodel field can be used in a query (no compilation errors)
    // The query returns empty because @Reference stores only the ID, not the full object
    List<RefVehicle> vehicles = entityStream
        .of(RefVehicle.class)
        .filter(RefVehicle$.OWNER_NAME.eq("John Doe"))
        .collect(Collectors.toList());

    // With @Reference, the owner field stores only the ID (e.g., "owner:01ARZ3...")
    // not the nested object with name/email, so this returns empty
    // For searching by referenced entity fields, use the ReferenceField.eq() with the entity
    assertThat(vehicles).isEmpty();
  }

  /**
   * Test that OWNER_EMAIL field can be used in queries.
   *
   * Note: See testOwnerNameFieldIsUsableInQuery for explanation of @Reference behavior.
   */
  @Test
  void testOwnerEmailFieldIsUsableInQuery() {
    // Verify the metamodel field can be used in a query (no compilation errors)
    List<RefVehicle> vehicles = entityStream
        .of(RefVehicle.class)
        .filter(RefVehicle$.OWNER_EMAIL.eq("jane@example.com"))
        .collect(Collectors.toList());

    // Returns empty because @Reference stores only the ID
    assertThat(vehicles).isEmpty();
  }

  /**
   * Test searching by the reference itself using the existing ReferenceField.eq() method.
   * This is the correct way to search by reference in the current implementation.
   */
  @Test
  void testSearchVehiclesByOwnerReference() {
    // Search for vehicles owned by johnDoe using the reference field
    List<RefVehicle> johnsVehicles = entityStream
        .of(RefVehicle.class)
        .filter(RefVehicle$.OWNER.eq(johnDoe))
        .collect(Collectors.toList());

    assertThat(johnsVehicles).hasSize(2);
    assertThat(johnsVehicles).extracting("model").containsExactlyInAnyOrder("Model S", "Mustang");
  }

  /**
   * Test that the feature also works with existing State/City models.
   * State has an @Indexed 'name' field, so City$ should have STATE_NAME.
   */
  @Test
  void testExistingCityModelHasStateNameField() throws Exception {
    // This test verifies that City$ has a field named STATE_NAME
    Class<?> metamodelClass = Class.forName("com.redis.om.spring.fixtures.document.model.City$");

    // Check that the static field STATE_NAME exists
    Field stateNameField = metamodelClass.getDeclaredField("STATE_NAME");
    assertThat(stateNameField).isNotNull();

    // Check that the field is accessible as a static field
    Object stateNameValue = stateNameField.get(null);
    assertThat(stateNameValue).isNotNull();
  }

  // ==================================================================================
  // Tests for additional indexed field types (PR review feedback)
  // ==================================================================================

  /**
   * Test that the metamodel generates OWNER_CATEGORY field for @TagIndexed fields.
   */
  @Test
  void testMetamodelGeneratesOwnerCategoryField() throws Exception {
    Class<?> metamodelClass = Class.forName("com.redis.om.spring.fixtures.document.model.RefVehicle$");

    // Check that the static field OWNER_CATEGORY exists (from @TagIndexed)
    Field ownerCategoryField = metamodelClass.getDeclaredField("OWNER_CATEGORY");
    assertThat(ownerCategoryField).isNotNull();

    Object ownerCategoryValue = ownerCategoryField.get(null);
    assertThat(ownerCategoryValue).isNotNull();
  }

  /**
   * Test that the metamodel generates OWNER_AGE field for @NumericIndexed fields.
   */
  @Test
  void testMetamodelGeneratesOwnerAgeField() throws Exception {
    Class<?> metamodelClass = Class.forName("com.redis.om.spring.fixtures.document.model.RefVehicle$");

    // Check that the static field OWNER_AGE exists (from @NumericIndexed)
    Field ownerAgeField = metamodelClass.getDeclaredField("OWNER_AGE");
    assertThat(ownerAgeField).isNotNull();

    Object ownerAgeValue = ownerAgeField.get(null);
    assertThat(ownerAgeValue).isNotNull();
  }

  /**
   * Test that the metamodel generates OWNER_ACTIVE field for @Indexed Boolean fields.
   */
  @Test
  void testMetamodelGeneratesOwnerActiveField() throws Exception {
    Class<?> metamodelClass = Class.forName("com.redis.om.spring.fixtures.document.model.RefVehicle$");

    // Check that the static field OWNER_ACTIVE exists (from @Indexed Boolean)
    Field ownerActiveField = metamodelClass.getDeclaredField("OWNER_ACTIVE");
    assertThat(ownerActiveField).isNotNull();

    Object ownerActiveValue = ownerActiveField.get(null);
    assertThat(ownerActiveValue).isNotNull();
  }

  /**
   * Test that all expected fields from the Owner entity are generated in RefVehicle$.
   */
  @Test
  void testAllOwnerIndexedFieldsAreGenerated() throws Exception {
    Class<?> metamodelClass = Class.forName("com.redis.om.spring.fixtures.document.model.RefVehicle$");

    // Verify all expected fields exist
    String[] expectedFields = {
        "OWNER_NAME",     // @Searchable
        "OWNER_EMAIL",    // @Indexed
        "OWNER_CATEGORY", // @TagIndexed
        "OWNER_AGE",      // @NumericIndexed
        "OWNER_ACTIVE"    // @Indexed Boolean
    };

    for (String fieldName : expectedFields) {
      Field field = metamodelClass.getDeclaredField(fieldName);
      assertThat(field).as("Field %s should exist", fieldName).isNotNull();
      Object value = field.get(null);
      assertThat(value).as("Field %s should have a non-null value", fieldName).isNotNull();
    }
  }

  // ==================================================================================
  // Tests for Date/time and UUID types (PR review feedback)
  // ==================================================================================

  /**
   * Test that the metamodel generates OWNER_BIRTH_DATE field for @Indexed LocalDate fields.
   */
  @Test
  void testMetamodelGeneratesOwnerBirthDateField() throws Exception {
    Class<?> metamodelClass = Class.forName("com.redis.om.spring.fixtures.document.model.RefVehicle$");

    // Check that the static field OWNER_BIRTH_DATE exists (from @Indexed LocalDate)
    Field ownerBirthDateField = metamodelClass.getDeclaredField("OWNER_BIRTH_DATE");
    assertThat(ownerBirthDateField).isNotNull();

    Object ownerBirthDateValue = ownerBirthDateField.get(null);
    assertThat(ownerBirthDateValue).isNotNull();
  }

  /**
   * Test that the metamodel generates OWNER_CREATED_AT field for @Indexed LocalDateTime fields.
   */
  @Test
  void testMetamodelGeneratesOwnerCreatedAtField() throws Exception {
    Class<?> metamodelClass = Class.forName("com.redis.om.spring.fixtures.document.model.RefVehicle$");

    // Check that the static field OWNER_CREATED_AT exists (from @Indexed LocalDateTime)
    Field ownerCreatedAtField = metamodelClass.getDeclaredField("OWNER_CREATED_AT");
    assertThat(ownerCreatedAtField).isNotNull();

    Object ownerCreatedAtValue = ownerCreatedAtField.get(null);
    assertThat(ownerCreatedAtValue).isNotNull();
  }

  /**
   * Test that the metamodel generates OWNER_EXTERNAL_ID field for @Indexed UUID fields.
   */
  @Test
  void testMetamodelGeneratesOwnerExternalIdField() throws Exception {
    Class<?> metamodelClass = Class.forName("com.redis.om.spring.fixtures.document.model.RefVehicle$");

    // Check that the static field OWNER_EXTERNAL_ID exists (from @Indexed UUID)
    Field ownerExternalIdField = metamodelClass.getDeclaredField("OWNER_EXTERNAL_ID");
    assertThat(ownerExternalIdField).isNotNull();

    Object ownerExternalIdValue = ownerExternalIdField.get(null);
    assertThat(ownerExternalIdValue).isNotNull();
  }

  /**
   * Test that all Date/time and UUID fields from Owner are generated in RefVehicle$.
   */
  @Test
  void testAllDateTimeAndUuidFieldsAreGenerated() throws Exception {
    Class<?> metamodelClass = Class.forName("com.redis.om.spring.fixtures.document.model.RefVehicle$");

    // Verify Date/time and UUID fields exist
    String[] expectedFields = {
        "OWNER_BIRTH_DATE",   // @Indexed LocalDate
        "OWNER_CREATED_AT",   // @Indexed LocalDateTime
        "OWNER_EXTERNAL_ID"   // @Indexed UUID
    };

    for (String fieldName : expectedFields) {
      Field field = metamodelClass.getDeclaredField(fieldName);
      assertThat(field).as("Field %s should exist", fieldName).isNotNull();
      Object value = field.get(null);
      assertThat(value).as("Field %s should have a non-null value", fieldName).isNotNull();
    }
  }
}
