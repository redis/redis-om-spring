package com.redis.om.spring.annotations.hash;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.document.model.Company;
import com.redis.om.spring.fixtures.hash.model.HashWithHashTagId;
import com.redis.om.spring.fixtures.hash.model.HashWithHashTagId$;
import com.redis.om.spring.fixtures.hash.repository.HashWithHashTagIdRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
class RedisHashWithIdFilterTest extends AbstractBaseEnhancedRedisTest {
  @Autowired
  HashWithHashTagIdRepository repository;

  @AfterEach
  public void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void testRepositorySaveAll() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.saveAll(Set.of(hwht1, hwht2, hwht3));

    assertThat(template.hasKey("hwht:{" + hwht1.getId() + "}")).isTrue();
    assertThat(template.hasKey("hwht:{" + hwht2.getId() + "}")).isTrue();
    assertThat(template.hasKey("hwht:{" + hwht3.getId() + "}")).isTrue();

    assertThat(template.hasKey("hwht:" + hwht1.getId())).isFalse();
    assertThat(template.hasKey("hwht:" + hwht2.getId())).isFalse();
    assertThat(template.hasKey("hwht:" + hwht3.getId())).isFalse();
  }

  @Test
  void testRepositorySave() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.save(hwht1);
    repository.save(hwht2);
    repository.save(hwht3);

    assertThat(template.hasKey("hwht:{" + hwht1.getId() + "}")).isTrue();
    assertThat(template.hasKey("hwht:{" + hwht2.getId() + "}")).isTrue();
    assertThat(template.hasKey("hwht:{" + hwht3.getId() + "}")).isTrue();

    assertThat(template.hasKey("hwht:" + hwht1.getId())).isFalse();
    assertThat(template.hasKey("hwht:" + hwht2.getId())).isFalse();
    assertThat(template.hasKey("hwht:" + hwht3.getId())).isFalse();
  }

  @Test
  void testRepositoryCount() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.saveAll(Set.of(hwht1, hwht2, hwht3));
    assertEquals(3, repository.count());
  }

  @Test
  void testRepositoryFindById() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.saveAll(Set.of(hwht1, hwht2, hwht3));

    Optional<HashWithHashTagId> maybehwht1 = repository.findById(hwht1.getId());
    Optional<HashWithHashTagId> maybehwht2 = repository.findById(hwht1.getId());
    Optional<HashWithHashTagId> maybehwht3 = repository.findById(hwht1.getId());

    assertTrue(maybehwht1.isPresent());
    assertTrue(maybehwht2.isPresent());
    assertTrue(maybehwht3.isPresent());
  }

  @Test
  void testFindAll() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.saveAll(Set.of(hwht1, hwht2, hwht3));

    List<HashWithHashTagId> all = repository.findAll();
    assertThat(all).hasSize(3);
  }


  @Test
  void testDeleteOne() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.saveAll(Set.of(hwht1, hwht2, hwht3));

    assertThat(repository.count()).isEqualTo(3);
    repository.delete(hwht1);
    assertThat(repository.count()).isEqualTo(2);
  }

  @Test
  void testGetIds() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.saveAll(Set.of(hwht1, hwht2, hwht3));
    Iterable<String> ids = repository.getIds();
    assertThat(ids).hasSize(3);
    assertThat(ids).contains(hwht1.getId(), hwht2.getId(), hwht3.getId());
  }

  @Test
  void testGetFieldsByIds() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.saveAll(Set.of(hwht1, hwht2, hwht3));

    Iterable<String> ids = List.of(hwht1.getId(), hwht2.getId(), hwht3.getId());
    Iterable<String> companyNames = repository.getFieldsByIds(ids, HashWithHashTagId$.NAME);
    assertThat(companyNames).containsExactly(hwht1.getName(), hwht2.getName(), hwht3.getName());
  }

  @Test
  void testGetId() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.saveAll(Set.of(hwht1, hwht2, hwht3));

    Iterable<String> ids = List.of(hwht1.getId(), hwht2.getId(), hwht3.getId());
    assertThat(repository.getIds()).containsAll(ids);
  }

  @Test
  void testExpirationGetSet() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.saveAll(Set.of(hwht1, hwht2, hwht3));
    repository.setExpiration(hwht1.getId(), 10L, TimeUnit.MINUTES);

    assertThat(repository.getExpiration(hwht1.getId())).isEqualTo(10L * 60);
  }

  @Test
  void testUpdateField() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.saveAll(Set.of(hwht1, hwht2, hwht3));
    repository.updateField(hwht1, HashWithHashTagId$.NAME, "Rocinante");
    Optional<HashWithHashTagId> maybehwht1 = repository.findById(hwht1.getId());
    assertAll( //
            () -> assertThat(maybehwht1).isPresent(), //
            () -> assertThat(maybehwht1.get().getName()).isEqualTo("Rocinante")//
    );
  }

  @Test
  void testDeleteAllById() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.saveAll(Set.of(hwht1, hwht2, hwht3));
    Iterable<String> ids = List.of(hwht1.getId(), hwht2.getId(), hwht3.getId());
    repository.deleteAllById(ids);
    assertThat(repository.count()).isEqualTo(0);
  }

  @Test
  void testDeleteAll() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.saveAll(Set.of(hwht1, hwht2, hwht3));
    assertThat(repository.count()).isEqualTo(3);
    repository.deleteAll();
    assertThat(repository.count()).isEqualTo(0);
  }

  @Test
  void testDeleteById() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.saveAll(Set.of(hwht1, hwht2, hwht3));
    assertThat(repository.count()).isEqualTo(3);
    repository.deleteById(hwht1.getId());
    repository.deleteById(hwht2.getId());
    assertThat(repository.count()).isEqualTo(1);
  }

  @Test
  void testFindAllById() {
    HashWithHashTagId hwht1 = HashWithHashTagId.of("hwht1");
    HashWithHashTagId hwht2 = HashWithHashTagId.of("hwht2");
    HashWithHashTagId hwht3 = HashWithHashTagId.of("hwht3");
    repository.saveAll(Set.of(hwht1, hwht2, hwht3));

    Iterable<String> ids = List.of(hwht1.getId(), hwht3.getId());
    assertThat(repository.findAllById(ids)).contains(hwht1, hwht3);
  }


}
