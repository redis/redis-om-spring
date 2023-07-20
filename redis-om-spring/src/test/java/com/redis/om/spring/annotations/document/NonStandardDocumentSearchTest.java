package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection") class NonStandardDocumentSearchTest extends AbstractBaseDocumentTest {
  @Autowired
  CustomRepository repository;

  @Autowired CompanyWithLongIdRepository companyRepo;

  @Autowired StringRedisTemplate template;

  private long id1;
  private long id2;

  @BeforeEach
  void loadTestData() {
    Custom c1 = Custom.of("foofoo");
    var nl1 = NestLevel1.of("nl-1-1", "Louis, I think this is the beginning of a beautiful friendship.", NestLevel2.of("nl-2-1", "Here's looking at you, kid."));
    c1.setNest_level1(nl1);
    Custom c2 = Custom.of("barbar");
    Custom c3 = Custom.of("bazbaz");
    c2.setTaken(true);

    repository.saveAll(List.of(c1, c2, c3));

    id1 = c1.getId();
    id2 = c2.getId();
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void testGetByName() {
    assertEquals(3, repository.count());
    List<Custom> customs = repository.searchByName("barbar");
    assertThat(customs).hasSize(1);
    Custom barbar = customs.get(0);
    assertThat(barbar.getName()).isEqualTo("barbar");
  }

  @Test
  void testBasicCrudOperations() {
    Optional<Custom> maybeC1 = repository.findById(id1);
    Optional<Custom> maybeC2 = repository.findById(id2);
    Optional<Custom> nonExistent = repository.findById(8675309L);

    assertTrue(maybeC1.isPresent());
    assertTrue(maybeC2.isPresent());
    assertTrue(nonExistent.isEmpty());

    // delete given an entity
    repository.delete(maybeC1.get());

    assertEquals(2, repository.count());

    // delete given an id
    repository.deleteById(id2);

    assertEquals(1, repository.count());
  }

  @Test
  void testDeleteAll() {
    assertThat(repository.count()).isEqualTo(3L);
    repository.deleteAll();
    assertThat(repository.count()).isZero();
  }
  
  @Test
  void testUpdateSingleField() {
    Optional<Custom> maybeC1 = repository.findById(id1);
    assertTrue(maybeC1.isPresent());
    repository.updateField(maybeC1.get(), Custom$.NAME, "fufoo");

    Optional<Custom> maybeC1After = repository.findById(id1);

    assertAll( //
        () -> assertTrue(maybeC1After.isPresent()), () -> assertEquals("fufoo", maybeC1After.get().getName()) //
    );
  }

  @Test
  void testUpdateDeepNestedNonJavaCompliantNamedField() {
    Optional<Custom> dn1 = repository.findById(id1);
    assertTrue(dn1.isPresent());
    repository.updateField(dn1.get(), Custom$.NEST_LEVEL1_NEST_LEVEL2_NAME, "dos-uno");

    Optional<Custom> dn1After = repository.findById(id1);

    assertAll( //
        () -> assertThat(dn1.get().getName()).isNotEqualTo("dos-uno"), //
        () -> assertTrue(dn1After.isPresent()), //
        () -> assertEquals("dos-uno", dn1After.get().getNest_level1().getNestLevel2().getName()) //
    );
  }

  @Test
  void testSaveAllWithNonStringKey() {
    CompanyWithLongId redis = CompanyWithLongId.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
        "stack@redis.com");

    CompanyWithLongId microsoft = CompanyWithLongId.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640160), "research@microsoft.com");

    companyRepo.saveAll(List.of(redis, microsoft));

    assertEquals(2, companyRepo.count());
  }

}
