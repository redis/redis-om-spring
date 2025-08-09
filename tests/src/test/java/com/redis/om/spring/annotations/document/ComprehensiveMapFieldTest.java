package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.ComprehensiveMapEntity;
import com.redis.om.spring.fixtures.document.repository.ComprehensiveMapEntityRepository;

import java.util.UUID;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import org.springframework.data.geo.Point;
import com.github.f4b6a3.ulid.Ulid;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Comprehensive test for all Map field value types in Redis OM Spring.
 * Tests indexing, querying, and retrieval for all supported Map value types.
 */
class ComprehensiveMapFieldTest extends AbstractBaseDocumentTest {

  @Autowired
  ComprehensiveMapEntityRepository repository;

  @BeforeEach
  void setUp() {
    repository.deleteAll();
  }

  @Test
  void testStringMapValues() {
    // Create entity with String Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("StringMapTest");
    entity.getStringValues().put("name", "John");
    entity.getStringValues().put("city", "San Francisco");
    entity.getStringValues().put("country", "USA");
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byValue1 = repository.findByStringValuesMapContains("John");
    List<ComprehensiveMapEntity> byValue2 = repository.findByStringValuesMapContains("San Francisco");
    List<ComprehensiveMapEntity> byValue3 = repository.findByStringValuesMapContains("USA");
    List<ComprehensiveMapEntity> byNonExistent = repository.findByStringValuesMapContains("NonExistent");
    
    assertThat(byValue1).hasSize(1).containsExactly(saved);
    assertThat(byValue2).hasSize(1).containsExactly(saved);
    assertThat(byValue3).hasSize(1).containsExactly(saved);
    assertThat(byNonExistent).isEmpty();
    
    // Verify retrieved entity has correct values
    ComprehensiveMapEntity retrieved = byValue1.get(0);
    assertThat(retrieved.getStringValues()).containsEntry("name", "John");
    assertThat(retrieved.getStringValues()).containsEntry("city", "San Francisco");
    assertThat(retrieved.getStringValues()).containsEntry("country", "USA");
  }

  @Test
  void testBooleanMapValues() {
    // Create entity with Boolean Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("BooleanMapTest");
    entity.getBooleanValues().put("isActive", true);
    entity.getBooleanValues().put("isVerified", false);
    entity.getBooleanValues().put("hasPermission", true);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byTrue = repository.findByBooleanValuesMapContains(true);
    List<ComprehensiveMapEntity> byFalse = repository.findByBooleanValuesMapContains(false);
    
    assertThat(byTrue).hasSize(1).containsExactly(saved);
    assertThat(byFalse).hasSize(1).containsExactly(saved);
    
    // Verify retrieved entity has correct values  
    ComprehensiveMapEntity retrieved = byTrue.get(0);
    assertThat(retrieved.getBooleanValues()).containsEntry("isActive", true);
    assertThat(retrieved.getBooleanValues()).containsEntry("isVerified", false);
    assertThat(retrieved.getBooleanValues()).containsEntry("hasPermission", true);
  }

  @Test
  void testUuidMapValues() {
    // Create entity with UUID Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("UuidMapTest");
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    entity.getUuidValues().put("user", uuid1);
    entity.getUuidValues().put("session", uuid2);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byUuid1 = repository.findByUuidValuesMapContains(uuid1);
    List<ComprehensiveMapEntity> byUuid2 = repository.findByUuidValuesMapContains(uuid2);
    UUID nonExistentUuid = UUID.randomUUID();
    List<ComprehensiveMapEntity> byNonExistent = repository.findByUuidValuesMapContains(nonExistentUuid);
    
    assertThat(byUuid1).hasSize(1).containsExactly(saved);
    assertThat(byUuid2).hasSize(1).containsExactly(saved);
    assertThat(byNonExistent).isEmpty();
    
    // Verify retrieved entity has correct values
    ComprehensiveMapEntity retrieved = byUuid1.get(0);
    assertThat(retrieved.getUuidValues()).containsEntry("user", uuid1);
    assertThat(retrieved.getUuidValues()).containsEntry("session", uuid2);
  }

  @Test
  void testUlidMapValues() {
    // Create entity with Ulid Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("UlidMapTest");
    Ulid ulid1 = Ulid.fast();
    Ulid ulid2 = Ulid.fast();
    entity.getUlidValues().put("request", ulid1);
    entity.getUlidValues().put("response", ulid2);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byUlid1 = repository.findByUlidValuesMapContains(ulid1);
    List<ComprehensiveMapEntity> byUlid2 = repository.findByUlidValuesMapContains(ulid2);
    
    assertThat(byUlid1).hasSize(1).containsExactly(saved);
    assertThat(byUlid2).hasSize(1).containsExactly(saved);
    
    // Verify retrieved entity has correct values
    ComprehensiveMapEntity retrieved = byUlid1.get(0);
    assertThat(retrieved.getUlidValues()).containsEntry("request", ulid1);
    assertThat(retrieved.getUlidValues()).containsEntry("response", ulid2);
  }

  @Test
  void testEnumMapValues() {
    // Create entity with Enum Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("EnumMapTest");
    entity.getEnumValues().put("status", ComprehensiveMapEntity.TestEnum.OPTION_A);
    entity.getEnumValues().put("priority", ComprehensiveMapEntity.TestEnum.OPTION_B);
    entity.getEnumValues().put("category", ComprehensiveMapEntity.TestEnum.OPTION_C);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byOptionA = repository.findByEnumValuesMapContains(ComprehensiveMapEntity.TestEnum.OPTION_A);
    List<ComprehensiveMapEntity> byOptionB = repository.findByEnumValuesMapContains(ComprehensiveMapEntity.TestEnum.OPTION_B);
    List<ComprehensiveMapEntity> byOptionC = repository.findByEnumValuesMapContains(ComprehensiveMapEntity.TestEnum.OPTION_C);
    
    assertThat(byOptionA).hasSize(1).containsExactly(saved);
    assertThat(byOptionB).hasSize(1).containsExactly(saved);
    assertThat(byOptionC).hasSize(1).containsExactly(saved);
    
    // Verify retrieved entity has correct values
    ComprehensiveMapEntity retrieved = byOptionA.get(0);
    assertThat(retrieved.getEnumValues()).containsEntry("status", ComprehensiveMapEntity.TestEnum.OPTION_A);
    assertThat(retrieved.getEnumValues()).containsEntry("priority", ComprehensiveMapEntity.TestEnum.OPTION_B);
    assertThat(retrieved.getEnumValues()).containsEntry("category", ComprehensiveMapEntity.TestEnum.OPTION_C);
  }

  @Test
  void testIntegerMapValues() {
    // Create entity with Integer Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("IntegerMapTest");
    entity.getIntegerValues().put("age", 25);
    entity.getIntegerValues().put("score", 100);
    entity.getIntegerValues().put("level", 5);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byAge = repository.findByIntegerValuesMapContains(25);
    List<ComprehensiveMapEntity> byScore = repository.findByIntegerValuesMapContains(100);
    List<ComprehensiveMapEntity> byLevel = repository.findByIntegerValuesMapContains(5);
    List<ComprehensiveMapEntity> byNonExistent = repository.findByIntegerValuesMapContains(999);
    
    assertThat(byAge).hasSize(1).containsExactly(saved);
    assertThat(byScore).hasSize(1).containsExactly(saved);
    assertThat(byLevel).hasSize(1).containsExactly(saved);
    assertThat(byNonExistent).isEmpty();
    
    // Test range queries
    List<ComprehensiveMapEntity> byGreaterThan = repository.findByIntegerValuesMapContainsGreaterThan(20);
    List<ComprehensiveMapEntity> byLessThan = repository.findByIntegerValuesMapContainsLessThan(30);
    
    assertThat(byGreaterThan).hasSize(1).containsExactly(saved);
    assertThat(byLessThan).hasSize(1).containsExactly(saved);
    
    // Verify retrieved entity has correct values
    ComprehensiveMapEntity retrieved = byAge.get(0);
    assertThat(retrieved.getIntegerValues()).containsEntry("age", 25);
    assertThat(retrieved.getIntegerValues()).containsEntry("score", 100);
    assertThat(retrieved.getIntegerValues()).containsEntry("level", 5);
  }

  @Test
  void testLongMapValues() {
    // Create entity with Long Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("LongMapTest");
    entity.getLongValues().put("timestamp", 1234567890L);
    entity.getLongValues().put("fileSize", 1048576L);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byTimestamp = repository.findByLongValuesMapContains(1234567890L);
    List<ComprehensiveMapEntity> byFileSize = repository.findByLongValuesMapContains(1048576L);
    
    assertThat(byTimestamp).hasSize(1).containsExactly(saved);
    assertThat(byFileSize).hasSize(1).containsExactly(saved);
    
    // Verify retrieved entity has correct values
    ComprehensiveMapEntity retrieved = byTimestamp.get(0);
    assertThat(retrieved.getLongValues()).containsEntry("timestamp", 1234567890L);
    assertThat(retrieved.getLongValues()).containsEntry("fileSize", 1048576L);
  }

  @Test
  void testDoubleMapValues() {
    // Create entity with Double Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("DoubleMapTest");
    entity.getDoubleValues().put("price", 99.99);
    entity.getDoubleValues().put("rating", 4.5);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byPrice = repository.findByDoubleValuesMapContains(99.99);
    List<ComprehensiveMapEntity> byRating = repository.findByDoubleValuesMapContains(4.5);
    
    assertThat(byPrice).hasSize(1).containsExactly(saved);
    assertThat(byRating).hasSize(1).containsExactly(saved);
    
    // Verify retrieved entity has correct values
    ComprehensiveMapEntity retrieved = byPrice.get(0);
    assertThat(retrieved.getDoubleValues()).containsEntry("price", 99.99);
    assertThat(retrieved.getDoubleValues()).containsEntry("rating", 4.5);
  }

  @Test
  void testFloatMapValues() {
    // Create entity with Float Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("FloatMapTest");
    entity.getFloatValues().put("temperature", 25.5f);
    entity.getFloatValues().put("humidity", 60.0f);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byTemp = repository.findByFloatValuesMapContains(25.5f);
    List<ComprehensiveMapEntity> byHumidity = repository.findByFloatValuesMapContains(60.0f);
    
    assertThat(byTemp).hasSize(1).containsExactly(saved);
    assertThat(byHumidity).hasSize(1).containsExactly(saved);
    
    // Verify retrieved entity has correct values
    ComprehensiveMapEntity retrieved = byTemp.get(0);
    assertThat(retrieved.getFloatValues()).containsEntry("temperature", 25.5f);
    assertThat(retrieved.getFloatValues()).containsEntry("humidity", 60.0f);
  }

  @Test
  void testBigDecimalMapValues() {
    // Create entity with BigDecimal Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("BigDecimalMapTest");
    BigDecimal amount = new BigDecimal("1234.56");
    BigDecimal balance = new BigDecimal("9876.54");
    entity.getBigDecimalValues().put("amount", amount);
    entity.getBigDecimalValues().put("balance", balance);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byAmount = repository.findByBigDecimalValuesMapContains(amount);
    List<ComprehensiveMapEntity> byBalance = repository.findByBigDecimalValuesMapContains(balance);
    
    assertThat(byAmount).hasSize(1).containsExactly(saved);
    assertThat(byBalance).hasSize(1).containsExactly(saved);
    
    // Verify retrieved entity has correct values
    ComprehensiveMapEntity retrieved = byAmount.get(0);
    assertThat(retrieved.getBigDecimalValues()).containsEntry("amount", amount);
    assertThat(retrieved.getBigDecimalValues()).containsEntry("balance", balance);
  }

  @Test
  void testLocalDateTimeMapValues() {
    // Create entity with LocalDateTime Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("LocalDateTimeMapTest");
    LocalDateTime created = LocalDateTime.now().minusDays(1);
    LocalDateTime updated = LocalDateTime.now();
    entity.getLocalDateTimeValues().put("created", created);
    entity.getLocalDateTimeValues().put("updated", updated);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byCreated = repository.findByLocalDateTimeValuesMapContains(created);
    List<ComprehensiveMapEntity> byUpdated = repository.findByLocalDateTimeValuesMapContains(updated);
    
    assertThat(byCreated).hasSize(1);
    assertThat(byUpdated).hasSize(1);
    assertThat(byCreated.get(0).getId()).isEqualTo(saved.getId());
    assertThat(byUpdated.get(0).getId()).isEqualTo(saved.getId());
    
    // Verify retrieved entity has correct values (with tolerance for precision loss)
    ComprehensiveMapEntity retrieved = byCreated.get(0);
    assertThat(retrieved.getLocalDateTimeValues()).containsKey("created");
    assertThat(retrieved.getLocalDateTimeValues()).containsKey("updated");
    
    // Check values are within 1 second tolerance due to serialization precision loss
    LocalDateTime retrievedCreated = retrieved.getLocalDateTimeValues().get("created");
    LocalDateTime retrievedUpdated = retrieved.getLocalDateTimeValues().get("updated");
    assertThat(retrievedCreated).isCloseTo(created, within(1, ChronoUnit.SECONDS));
    assertThat(retrievedUpdated).isCloseTo(updated, within(1, ChronoUnit.SECONDS));
  }

  @Test
  void testLocalDateMapValues() {
    // Create entity with LocalDate Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("LocalDateMapTest");
    LocalDate startDate = LocalDate.of(2023, 1, 1);
    LocalDate endDate = LocalDate.of(2023, 12, 31);
    entity.getLocalDateValues().put("start", startDate);
    entity.getLocalDateValues().put("end", endDate);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byStart = repository.findByLocalDateValuesMapContains(startDate);
    List<ComprehensiveMapEntity> byEnd = repository.findByLocalDateValuesMapContains(endDate);
    
    assertThat(byStart).hasSize(1).containsExactly(saved);
    assertThat(byEnd).hasSize(1).containsExactly(saved);
    
    // Verify retrieved entity has correct values
    ComprehensiveMapEntity retrieved = byStart.get(0);
    assertThat(retrieved.getLocalDateValues()).containsEntry("start", startDate);
    assertThat(retrieved.getLocalDateValues()).containsEntry("end", endDate);
  }

  @Test
  void testDateMapValues() {
    // Create entity with Date Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("DateMapTest");
    Date date1 = new Date(System.currentTimeMillis() - 86400000); // Yesterday
    Date date2 = new Date(); // Now
    entity.getDateValues().put("yesterday", date1);
    entity.getDateValues().put("today", date2);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byDate1 = repository.findByDateValuesMapContains(date1);
    List<ComprehensiveMapEntity> byDate2 = repository.findByDateValuesMapContains(date2);
    
    assertThat(byDate1).hasSize(1);
    assertThat(byDate2).hasSize(1);
    assertThat(byDate1.get(0).getId()).isEqualTo(saved.getId());
    assertThat(byDate2.get(0).getId()).isEqualTo(saved.getId());
    
    // Verify retrieved entity has correct values (with tolerance for precision loss)
    ComprehensiveMapEntity retrieved = byDate1.get(0);
    assertThat(retrieved.getDateValues()).containsKey("yesterday");
    assertThat(retrieved.getDateValues()).containsKey("today");
    
    // Check values are within 1 second tolerance due to serialization precision loss
    Date retrievedYesterday = retrieved.getDateValues().get("yesterday");
    Date retrievedToday = retrieved.getDateValues().get("today");
    assertThat(Math.abs(retrievedYesterday.getTime() - date1.getTime())).isLessThan(1000);
    assertThat(Math.abs(retrievedToday.getTime() - date2.getTime())).isLessThan(1000);
  }

  @Test
  void testInstantMapValues() {
    // Create entity with Instant Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("InstantMapTest");
    Instant instant1 = Instant.now().minusSeconds(3600); // 1 hour ago
    Instant instant2 = Instant.now();
    entity.getInstantValues().put("past", instant1);
    entity.getInstantValues().put("present", instant2);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byPast = repository.findByInstantValuesMapContains(instant1);
    List<ComprehensiveMapEntity> byPresent = repository.findByInstantValuesMapContains(instant2);
    
    assertThat(byPast).hasSize(1);
    assertThat(byPresent).hasSize(1);
    assertThat(byPast.get(0).getId()).isEqualTo(saved.getId());
    assertThat(byPresent.get(0).getId()).isEqualTo(saved.getId());
    
    // Verify retrieved entity has correct values (with tolerance for precision loss)
    ComprehensiveMapEntity retrieved = byPast.get(0);
    assertThat(retrieved.getInstantValues()).containsKey("past");
    assertThat(retrieved.getInstantValues()).containsKey("present");
    
    // Check values are within 1 second tolerance due to serialization precision loss
    Instant retrievedPast = retrieved.getInstantValues().get("past");
    Instant retrievedPresent = retrieved.getInstantValues().get("present");
    assertThat(retrievedPast).isCloseTo(instant1, within(1, ChronoUnit.SECONDS));
    assertThat(retrievedPresent).isCloseTo(instant2, within(1, ChronoUnit.SECONDS));
  }

  @Test
  void testOffsetDateTimeMapValues() {
    // Create entity with OffsetDateTime Map values  
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("OffsetDateTimeMapTest");
    OffsetDateTime odt1 = OffsetDateTime.now().minusDays(1);
    OffsetDateTime odt2 = OffsetDateTime.now();
    entity.getOffsetDateTimeValues().put("yesterday", odt1);
    entity.getOffsetDateTimeValues().put("today", odt2);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> byYesterday = repository.findByOffsetDateTimeValuesMapContains(odt1);
    List<ComprehensiveMapEntity> byToday = repository.findByOffsetDateTimeValuesMapContains(odt2);
    
    assertThat(byYesterday).hasSize(1);
    assertThat(byToday).hasSize(1);
    assertThat(byYesterday.get(0).getId()).isEqualTo(saved.getId());
    assertThat(byToday.get(0).getId()).isEqualTo(saved.getId());
    
    // Verify retrieved entity has correct values (with tolerance for precision loss)
    ComprehensiveMapEntity retrieved = byYesterday.get(0);
    assertThat(retrieved.getOffsetDateTimeValues()).containsKey("yesterday");
    assertThat(retrieved.getOffsetDateTimeValues()).containsKey("today");
    
    // Check values are within 1 second tolerance due to serialization precision loss
    OffsetDateTime retrievedYesterday = retrieved.getOffsetDateTimeValues().get("yesterday");
    OffsetDateTime retrievedToday = retrieved.getOffsetDateTimeValues().get("today");
    assertThat(retrievedYesterday).isCloseTo(odt1, within(1, ChronoUnit.SECONDS));
    assertThat(retrievedToday).isCloseTo(odt2, within(1, ChronoUnit.SECONDS));
  }

  @Test
  void testPointMapValues() {
    // Create entity with Point (GEO) Map values
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("PointMapTest");
    Point sanFrancisco = new Point(-122.4194, 37.7749);
    Point newYork = new Point(-74.0059, 40.7128);
    entity.getPointValues().put("sf", sanFrancisco);
    entity.getPointValues().put("nyc", newYork);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test repository queries
    List<ComprehensiveMapEntity> bySF = repository.findByPointValuesMapContains(sanFrancisco);
    List<ComprehensiveMapEntity> byNYC = repository.findByPointValuesMapContains(newYork);
    
    assertThat(bySF).hasSize(1).containsExactly(saved);
    assertThat(byNYC).hasSize(1).containsExactly(saved);
    
    // Verify retrieved entity has correct values
    ComprehensiveMapEntity retrieved = bySF.get(0);
    assertThat(retrieved.getPointValues()).containsEntry("sf", sanFrancisco);
    assertThat(retrieved.getPointValues()).containsEntry("nyc", newYork);
  }

  @Test
  void testMultipleMapTypesInSingleEntity() {
    // Create entity with multiple Map field types
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("MultiMapTest");
    
    // Add values for different Map types
    entity.getStringValues().put("name", "MultiTest");
    entity.getBooleanValues().put("active", true);
    entity.getIntegerValues().put("count", 42);
    entity.getDoubleValues().put("rating", 4.8);
    UUID uuid = UUID.randomUUID();
    entity.getUuidValues().put("id", uuid);
    entity.getEnumValues().put("status", ComprehensiveMapEntity.TestEnum.OPTION_A);
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Test queries for each type
    List<ComprehensiveMapEntity> byString = repository.findByStringValuesMapContains("MultiTest");
    List<ComprehensiveMapEntity> byBoolean = repository.findByBooleanValuesMapContains(true);
    List<ComprehensiveMapEntity> byInteger = repository.findByIntegerValuesMapContains(42);
    List<ComprehensiveMapEntity> byDouble = repository.findByDoubleValuesMapContains(4.8);
    List<ComprehensiveMapEntity> byUuid = repository.findByUuidValuesMapContains(uuid);
    List<ComprehensiveMapEntity> byEnum = repository.findByEnumValuesMapContains(ComprehensiveMapEntity.TestEnum.OPTION_A);
    
    // All queries should return the same entity
    assertThat(byString).hasSize(1).containsExactly(saved);
    assertThat(byBoolean).hasSize(1).containsExactly(saved);
    assertThat(byInteger).hasSize(1).containsExactly(saved);
    assertThat(byDouble).hasSize(1).containsExactly(saved);
    assertThat(byUuid).hasSize(1).containsExactly(saved);
    assertThat(byEnum).hasSize(1).containsExactly(saved);
    
    // Verify retrieved entity has all values
    ComprehensiveMapEntity retrieved = byString.get(0);
    assertThat(retrieved.getStringValues()).containsEntry("name", "MultiTest");
    assertThat(retrieved.getBooleanValues()).containsEntry("active", true);
    assertThat(retrieved.getIntegerValues()).containsEntry("count", 42);
    assertThat(retrieved.getDoubleValues()).containsEntry("rating", 4.8);
    assertThat(retrieved.getUuidValues()).containsEntry("id", uuid);
    assertThat(retrieved.getEnumValues()).containsEntry("status", ComprehensiveMapEntity.TestEnum.OPTION_A);
  }

  @Test
  void testEmptyMapValues() {
    // Create entity with empty Maps  
    ComprehensiveMapEntity entity = ComprehensiveMapEntity.of("EmptyMapTest");
    // Don't add any Map values - should remain empty
    
    ComprehensiveMapEntity saved = repository.save(entity);
    
    // Verify entity was saved and can be retrieved by name
    List<ComprehensiveMapEntity> byName = repository.findByName("EmptyMapTest");
    assertThat(byName).hasSize(1).containsExactly(saved);
    
    // Verify all Maps are empty
    ComprehensiveMapEntity retrieved = byName.get(0);
    assertThat(retrieved.getStringValues()).isEmpty();
    assertThat(retrieved.getBooleanValues()).isEmpty();
    assertThat(retrieved.getUuidValues()).isEmpty();
    assertThat(retrieved.getUlidValues()).isEmpty();
    assertThat(retrieved.getEnumValues()).isEmpty();
    assertThat(retrieved.getIntegerValues()).isEmpty();
    assertThat(retrieved.getLongValues()).isEmpty();
    assertThat(retrieved.getDoubleValues()).isEmpty();
    assertThat(retrieved.getFloatValues()).isEmpty();
    assertThat(retrieved.getBigDecimalValues()).isEmpty();
    assertThat(retrieved.getLocalDateTimeValues()).isEmpty();
    assertThat(retrieved.getLocalDateValues()).isEmpty();
    assertThat(retrieved.getDateValues()).isEmpty();
    assertThat(retrieved.getInstantValues()).isEmpty();
    assertThat(retrieved.getOffsetDateTimeValues()).isEmpty();
    assertThat(retrieved.getPointValues()).isEmpty();
  }
}