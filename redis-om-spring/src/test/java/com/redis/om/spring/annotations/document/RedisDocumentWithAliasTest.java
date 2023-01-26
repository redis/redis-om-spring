package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.Direccion;
import com.redis.om.spring.annotations.document.fixtures.WithAlias;
import com.redis.om.spring.annotations.document.fixtures.WithAliasRepository;
import com.redis.om.spring.ops.RedisModulesOperations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpellCheckingInspection") class RedisDocumentWithAliasTest extends AbstractBaseDocumentTest {
  @Autowired
  WithAliasRepository repository;

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  private String id1;

  @BeforeEach
  void loadTestData() {
    Point point1 = new Point(-122.124500, 47.640160);
    Set<String> tags = new HashSet<>();
    tags.add("news");
    tags.add("article");

    WithAlias doc1 = WithAlias.of("Oye man", tags, point1, 42, Direccion.of("David", "Called F. Sur"));
    doc1 = repository.save(doc1);

    id1 = doc1.getId();

    Point point2 = new Point(-122.066540, 37.377690);
    Set<String> tags2 = new HashSet<>();
    tags2.add("noticias");
    tags2.add("articulo");

    WithAlias doc2 = WithAlias.of("Epa chamo", tags2, point2, 99, Direccion.of("Valencia", "Called Libra"));
    repository.save(doc2);
  }

  @AfterEach
  public void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void testBasicCrudOperations() {
    Optional<WithAlias> maybeDoc1 = repository.findById(id1);
    assertTrue(maybeDoc1.isPresent());
    WithAlias doc1 = maybeDoc1.get();

    Optional<WithAlias> alsoMaybeDoc1 = repository.findFirstByNumber(42);
    assertTrue(alsoMaybeDoc1.isPresent());
    WithAlias alsoDoc1 = alsoMaybeDoc1.get();

    assertThat(alsoDoc1.getText()).isEqualTo(doc1.getText());
  }

}
