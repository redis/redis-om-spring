package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.Company;
import com.redis.om.spring.fixtures.document.model.CompanyMeta;
import com.redis.om.spring.fixtures.document.model.DunnageEntity;
import com.redis.om.spring.fixtures.document.model.MyDoc;
import com.redis.om.spring.fixtures.document.repository.CompanyRepository;
import com.redis.om.spring.fixtures.document.repository.DunnageEntityRepository;
import com.redis.om.spring.fixtures.document.repository.MyDocRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.geo.Point;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class EntityStreamDocumentQBEAdvancedTest extends AbstractBaseDocumentTest {
  @Autowired
  DunnageEntityRepository repository;

  @Autowired
  EntityStream entityStream;

  @BeforeEach
  void loadTestData() {
    repository.deleteAll();

    List<DunnageEntity> entities = new ArrayList<>();

    // Data from the screenshot
    entities.add(createDunnageEntity("TMMFV", "T423", "F3483", "CTCI", "THERMOFORME", "NO", 0, 0f, "EUR", null, null, null));
    entities.add(createDunnageEntity("TMMFV", "F375", "F2413", "PPO", "CORREX + EPP", "1150X350X202", 750, 36.71f, "EUR", null, null, null));
    entities.add(createDunnageEntity("TMMFV", "C035", "F1503", "JAMES LEIGH", "FOAM", "WHITE FOAM", 690, 14.1f, "GBP", null, null, null));
    entities.add(createDunnageEntity("TMMFV", "F519", "F3090", "CARPENTER", "FOAM", "NO", 100, 0f, "EURO", null, null, null));
    entities.add(createDunnageEntity("TMMFV", "J066", "20711", "FUJITSU TEN", "CARDBOARD DUNNAGE", "SPEAKER ASSY, RADIO", 100, 1f, "EURO", null, null, null));
    entities.add(createDunnageEntity("TMMFV", "S119", "F2168", "KAYSERSBERG", "TEXT EFFITEK+EVOLON", "540X340X290", 400, 25.56f, "EUR", null, null, null));
    entities.add(createDunnageEntity("TMMFV", "F332", "F3090", "CARPENTER", "FOAM", "4611 OL 15", 200, 9.59f, "EURO", null, null, null));
    entities.add(createDunnageEntity("TMMFV", "T103", "P2271", "FM BRADFORD", "THERMOFORMED", "BLACK PS", 630, 12f, "EUR", null, null, null));
    entities.add(createDunnageEntity("TMMFV", "F051", "P8489", "TMEM ADAPADZARI", "FOAM", "DIVIDER + BOTTOM", 200, 10f, "EUR", null, null, null));
    entities.add(createDunnageEntity("TMMFV", "F071", "P4750", "TRI", "FOAM", "GREY FOAM", 0, 7f, "EUR", null, null, null));

    repository.saveAll(entities);
  }

  @Test
  void testFindAllByExampleWithDunnageCodeContaining() {
    // Test searching for dunnageCode containing "F3" (using min 2 chars for prefix)
    DunnageEntity template = DunnageEntity.builder()
        .dunnageCode("F3")
        .build();

    ExampleMatcher matcher = ExampleMatcher.matching()
        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
        .withIgnoreNullValues();

    Example<DunnageEntity> example = Example.of(template, matcher);

    // Using entity stream
    List<DunnageEntity> results = entityStream.of(DunnageEntity.class)
        .filter(example)
        .collect(Collectors.toList());

    // Should find F375, F332 (both contain "F3")
    assertThat(results).hasSize(2);
    assertThat(results).extracting("dunnageCode")
        .containsExactlyInAnyOrder("F375", "F332");
  }

  @Test
  void testFindAllByExampleWithMaterialExactMatch() {
    // Test searching for material exactly matching "FOAM"
    DunnageEntity template = DunnageEntity.builder()
        .material("FOAM")
        .build();

    ExampleMatcher matcher = ExampleMatcher.matching()
        .withStringMatcher(ExampleMatcher.StringMatcher.EXACT)
        .withIgnoreNullValues();

    Example<DunnageEntity> example = Example.of(template, matcher);

    // Using entity stream
    List<DunnageEntity> results = entityStream.of(DunnageEntity.class)
        .filter(example)
        .collect(Collectors.toList());

    // Should find entries with exactly "FOAM" in material field
    assertThat(results).hasSize(5);
    assertThat(results).extracting("dunnageCode")
        .containsExactlyInAnyOrder("C035", "F519", "F332", "F051", "F071");
  }

  @Test
  void testSearchWithIgnoreCase() {
    // Test case-insensitive search
    DunnageEntity template = DunnageEntity.builder()
        .description("foam") // lowercase search term
        .build();

    ExampleMatcher matcher = ExampleMatcher.matching()
        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
        .withIgnoreCase()
        .withIgnoreNullValues();

    Example<DunnageEntity> example = Example.of(template, matcher);

    // Using entity stream
    List<DunnageEntity> results = entityStream.of(DunnageEntity.class)
        .filter(example)
        .collect(Collectors.toList());

    // Should find entries with "FOAM" in description field despite case difference
    assertThat(results).hasSizeGreaterThanOrEqualTo(2);
    assertThat(results).extracting("description")
        .contains("WHITE FOAM", "GREY FOAM");
  }

  @Test
  void testExactCustomerMatcherPattern() {
    // Create test parameters
    String plant = "TMMFV";
    String dunnageCode = "F3";         // @Searchable with CONTAINING
    String dunnageSuppcode = null;
    String dunnageSupplier = "CA";     // @Indexed
    String material = "FO";            // @Searchable with CONTAINING
    Integer weight = 100;              // @Indexed numeric
    String description = null;
    Float unitCost = null;
    String currency = null;
    String comment1 = null;
    String comment2 = null;
    String dunnagePiece = null;

    // Test with both AND and OR semantics
    boolean[] orValues = {false, true};

    for (boolean orBetweenParameters : orValues) {
      // Build entity just like in customer code
      DunnageEntity entity = DunnageEntity.builder()
          .plant(plant)
          .dunnageCode(dunnageCode)
          .dunnageSuppcode(dunnageSuppcode)
          .dunnageSupplier(dunnageSupplier)
          .material(material)
          .description(description)
          .weight(weight)
          .unitCost(unitCost)
          .currency(currency)
          .comment1(comment1)
          .comment2(comment2)
          .dunnagePiece(dunnagePiece)
          .build();

      // This is EXACTLY the pattern from customer code
      ExampleMatcher matcher = orBetweenParameters ? ExampleMatcher.matchingAny() : ExampleMatcher.matching();
      matcher = matcher
          .withIgnoreCase()
          .withIgnoreNullValues()
          .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

      Example<DunnageEntity> example = Example.of(entity, matcher);

      // Using entity stream
      List<DunnageEntity> results = entityStream.of(DunnageEntity.class)
          .filter(example)
          .collect(Collectors.toList());

      System.out.println("Results with orBetweenParameters=" + orBetweenParameters + ": " + results.size());

      // Log the results for debugging
      for (DunnageEntity result : results) {
        System.out.println("  Match: " + result.getDunnageCode() +
            ", Supplier: " + result.getDunnageSupplier() +
            ", Material: " + result.getMaterial() +
            ", Weight: " + result.getWeight());
      }

      // Verify based on search semantics
      if (orBetweenParameters) {
        // With OR semantics, should match any entity with any of the criteria
        assertThat(results.size()).isGreaterThan(1);
      } else {
        // With AND semantics, should match entities with all criteria
        // Our test data may not have exact matches for all criteria
        if (!results.isEmpty()) {
          // If we have matches, verify they satisfy all the criteria
          for (DunnageEntity result : results) {
            assertThat(result.getPlant()).isEqualTo(plant);
            assertThat(result.getDunnageCode()).contains(dunnageCode);
            assertThat(result.getDunnageSupplier()).contains(dunnageSupplier);
            assertThat(result.getMaterial()).contains(material);
            assertThat(result.getWeight()).isEqualTo(weight);
          }
        }
      }
    }
  }

  private DunnageEntity createDunnageEntity(String plant, String dunnageCode, String dunnageSuppcode,
      String dunnageSupplier, String material, String description,
      Integer weight, Float unitCost, String currency,
      String comment1, String comment2, String dunnagePiece) {

    String id = String.valueOf(DunnageEntity.generateId(plant, dunnageCode, dunnageSuppcode));

    return DunnageEntity.builder()
        .id(id)
        .plant(plant)
        .dunnageCode(dunnageCode)
        .dunnageSuppcode(dunnageSuppcode)
        .dunnageSupplier(dunnageSupplier)
        .material(material)
        .description(description)
        .weight(weight)
        .unitCost(unitCost)
        .currency(currency)
        .comment1(comment1)
        .comment2(comment2)
        .dunnagePiece(dunnagePiece)
        .build();
  }

}
