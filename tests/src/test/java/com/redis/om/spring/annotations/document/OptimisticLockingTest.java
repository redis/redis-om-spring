package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.VersionedEntity;
import com.redis.om.spring.fixtures.document.repository.VersionedEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OptimisticLockingTest extends AbstractBaseDocumentTest {
  @Autowired
  VersionedEntityRepository repository;

  @BeforeEach
  void before() {
    repository.deleteAll();
  }

  @Test
  void shouldInsertVersioned() {
    VersionedEntity versionedEntity = new VersionedEntity(41);

    VersionedEntity saved = repository.save(versionedEntity);
    Optional<VersionedEntity> maybeLoaded = repository.findById(41L);
    assertThat(maybeLoaded).isPresent();
    VersionedEntity loaded = maybeLoaded.get();

    assertThat(saved.getVersion()).isEqualTo(1);
    assertThat(loaded).isNotNull();
    assertThat(loaded.getVersion()).isEqualTo(1);
  }

  @Test
  void duplicateInsertShouldFail() {
    repository.save(new VersionedEntity(44));

    assertThatThrownBy(() -> repository.save(new VersionedEntity(44))).isInstanceOf(DuplicateKeyException.class);
  }

  @Test
  void shouldUpdateVersioned() {
    VersionedEntity versionedEntity = new VersionedEntity(40);
    VersionedEntity saved = repository.save(versionedEntity);
    assertThat(saved.getVersion()).isEqualTo(1);
    VersionedEntity updated = repository.update(saved);
    assertThat(updated.getVersion()).isEqualTo(2);
    Optional<VersionedEntity> maybeLoaded = repository.findById(40L);
    assertThat(maybeLoaded).isPresent();
    VersionedEntity loaded = maybeLoaded.get();
    assertThat(loaded).isNotNull();
    assertThat(loaded.getVersion()).isEqualTo(2);
  }

  @Test
  void updateForOutdatedEntityShouldFail() {

    VersionedEntity versionedEntity = new VersionedEntity(42);

    repository.save(versionedEntity);

    assertThatThrownBy(() -> repository.save(new VersionedEntity(42, 5, "f"))).isInstanceOf(
        OptimisticLockingFailureException.class);
  }

  @Test
  void shouldDeleteVersionedEntity() {

    VersionedEntity versionedEntity = new VersionedEntity(43);

    VersionedEntity saved = repository.save(versionedEntity);

    repository.delete(saved);

    Optional<VersionedEntity> maybeLoaded = repository.findById(43L);
    assertThat(maybeLoaded).isEmpty();
  }

  @Test
  void deleteForOutdatedEntityShouldFail() {

    repository.save(new VersionedEntity(45));

    assertThatThrownBy(() -> repository.delete(new VersionedEntity(45))).isInstanceOf(
        OptimisticLockingFailureException.class);

    Optional<VersionedEntity> maybeLoaded = repository.findById(45L);
    assertThat(maybeLoaded).isPresent();
  }

}
