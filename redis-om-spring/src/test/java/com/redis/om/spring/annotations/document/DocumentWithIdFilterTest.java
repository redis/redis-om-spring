package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.DocWithHashTagId;
import com.redis.om.spring.fixtures.document.model.DocWithHashTagId$;
import com.redis.om.spring.fixtures.document.repository.DocWithHashTagIdRepository;
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
class DocumentWithIdFilterTest extends AbstractBaseDocumentTest {
  @Autowired
  DocWithHashTagIdRepository repository;

  @AfterEach
  public void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void testRepositorySaveAll() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.saveAll(Set.of(dwht1, dwht2, dwht3));

    assertThat(template.hasKey("dwht:{" + dwht1.getId() + "}")).isTrue();
    assertThat(template.hasKey("dwht:{" + dwht2.getId() + "}")).isTrue();
    assertThat(template.hasKey("dwht:{" + dwht3.getId() + "}")).isTrue();

    assertThat(template.hasKey("dwht:" + dwht1.getId())).isFalse();
    assertThat(template.hasKey("dwht:" + dwht2.getId())).isFalse();
    assertThat(template.hasKey("dwht:" + dwht3.getId())).isFalse();
  }

  @Test
  void testRepositorySave() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.save(dwht1);
    repository.save(dwht2);
    repository.save(dwht3);

    assertThat(template.hasKey("dwht:{" + dwht1.getId() + "}")).isTrue();
    assertThat(template.hasKey("dwht:{" + dwht2.getId() + "}")).isTrue();
    assertThat(template.hasKey("dwht:{" + dwht3.getId() + "}")).isTrue();

    assertThat(template.hasKey("dwht:" + dwht1.getId())).isFalse();
    assertThat(template.hasKey("dwht:" + dwht2.getId())).isFalse();
    assertThat(template.hasKey("dwht:" + dwht3.getId())).isFalse();
  }

  @Test
  void testRepositoryCount() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.saveAll(Set.of(dwht1, dwht2, dwht3));
    assertEquals(3, repository.count());
  }

  @Test
  void testRepositoryFindById() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.saveAll(Set.of(dwht1, dwht2, dwht3));

    Optional<DocWithHashTagId> maybedwht1 = repository.findById(dwht1.getId());
    Optional<DocWithHashTagId> maybedwht2 = repository.findById(dwht1.getId());
    Optional<DocWithHashTagId> maybedwht3 = repository.findById(dwht1.getId());

    assertTrue(maybedwht1.isPresent());
    assertTrue(maybedwht2.isPresent());
    assertTrue(maybedwht3.isPresent());
  }

  @Test
  void testFindAll() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.saveAll(Set.of(dwht1, dwht2, dwht3));

    List<DocWithHashTagId> all = repository.findAll();
    assertThat(all).hasSize(3);
  }


  @Test
  void testDeleteOne() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.saveAll(Set.of(dwht1, dwht2, dwht3));

    assertThat(repository.count()).isEqualTo(3);
    repository.delete(dwht1);
    assertThat(repository.count()).isEqualTo(2);
  }

  @Test
  void testGetIds() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.saveAll(Set.of(dwht1, dwht2, dwht3));
    Iterable<String> ids = repository.getIds();
    assertThat(ids).hasSize(3);
    assertThat(ids).contains(dwht1.getId(), dwht2.getId(), dwht3.getId());
  }

  @Test
  void testGetFieldsByIds() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.saveAll(Set.of(dwht1, dwht2, dwht3));

    Iterable<String> ids = List.of(dwht1.getId(), dwht2.getId(), dwht3.getId());
    Iterable<String> companyNames = repository.getFieldsByIds(ids, DocWithHashTagId$.NAME);
    assertThat(companyNames).containsExactly(dwht1.getName(), dwht2.getName(), dwht3.getName());
  }

  @Test
  void testGetId() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.saveAll(Set.of(dwht1, dwht2, dwht3));

    Iterable<String> ids = List.of(dwht1.getId(), dwht2.getId(), dwht3.getId());
    assertThat(repository.getIds()).containsAll(ids);
  }

  @Test
  void testExpirationGetSet() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.saveAll(Set.of(dwht1, dwht2, dwht3));
    repository.setExpiration(dwht1.getId(), 10L, TimeUnit.MINUTES);

    assertThat(repository.getExpiration(dwht1.getId())).isEqualTo(10L * 60);
  }

  @Test
  void testUpdateField() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.saveAll(Set.of(dwht1, dwht2, dwht3));
    repository.updateField(dwht1, DocWithHashTagId$.NAME, "Rocinante");
    Optional<DocWithHashTagId> maybedwht1 = repository.findById(dwht1.getId());
    assertAll( //
            () -> assertThat(maybedwht1).isPresent(), //
            () -> assertThat(maybedwht1.get().getName()).isEqualTo("Rocinante")//
    );
  }

  @Test
  void testDeleteAllById() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.saveAll(Set.of(dwht1, dwht2, dwht3));
    Iterable<String> ids = List.of(dwht1.getId(), dwht2.getId(), dwht3.getId());
    repository.deleteAllById(ids);
    assertThat(repository.count()).isEqualTo(0);
  }

  @Test
  void testDeleteAll() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.saveAll(Set.of(dwht1, dwht2, dwht3));
    assertThat(repository.count()).isEqualTo(3);
    repository.deleteAll();
    assertThat(repository.count()).isEqualTo(0);
  }

  @Test
  void testDeleteById() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.saveAll(Set.of(dwht1, dwht2, dwht3));
    assertThat(repository.count()).isEqualTo(3);
    repository.deleteById(dwht1.getId());
    repository.deleteById(dwht2.getId());
    assertThat(repository.count()).isEqualTo(1);
  }

  @Test
  void testFindAllById() {
    DocWithHashTagId dwht1 = DocWithHashTagId.of("dwht1");
    DocWithHashTagId dwht2 = DocWithHashTagId.of("dwht2");
    DocWithHashTagId dwht3 = DocWithHashTagId.of("dwht3");
    repository.saveAll(Set.of(dwht1, dwht2, dwht3));

    Iterable<String> ids = List.of(dwht1.getId(), dwht3.getId());
    assertThat(repository.findAllById(ids)).contains(dwht1, dwht3);
  }


}
