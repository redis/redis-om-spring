package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.Custom;
import com.redis.om.spring.annotations.document.fixtures.CustomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpellCheckingInspection") class NonStandardDocumentSearchTest extends AbstractBaseDocumentTest {
  @Autowired
  CustomRepository repository;

  @Autowired StringRedisTemplate template;

  private long id1;
  private long id2;

  @BeforeEach
  void loadTestData() {
    Custom c1 = Custom.of("foofoo");
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

}
