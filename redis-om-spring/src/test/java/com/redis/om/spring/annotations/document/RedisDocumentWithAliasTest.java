package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;

import com.google.gson.JsonObject;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.Direccion;
import com.redis.om.spring.annotations.document.fixtures.WithAlias;
import com.redis.om.spring.annotations.document.fixtures.WithAliasRepository;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;

class RedisDocumentWithAliasTest extends AbstractBaseDocumentTest {
  @Autowired
  WithAliasRepository repository;

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  String id1;
  String id2;

  @BeforeEach
  void loadTestData() {
    Point point1 = new Point(-122.124500, 47.640160);
    Set<String> tags = new HashSet<String>();
    tags.add("news");
    tags.add("article");

    WithAlias doc1 = WithAlias.of("Oye man", tags, point1, 42, Direccion.of("David", "Called F. Sur"));
    doc1 = repository.save(doc1);

    id1 = doc1.getId();

    Point point2 = new Point(-122.066540, 37.377690);
    Set<String> tags2 = new HashSet<String>();
    tags2.add("noticias");
    tags2.add("articulo");

    WithAlias doc2 = WithAlias.of("Epa chamo", tags2, point2, 99, Direccion.of("Valencia", "Called Libra"));
    doc2 = repository.save(doc2);

    id2 = doc2.getId();
  }

  @AfterEach
  public void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void testBasicCrudOperations() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();

    Optional<WithAlias> maybeDoc1 = repository.findById(id1);
    assertTrue(maybeDoc1.isPresent());
    WithAlias doc1 = maybeDoc1.get();

    JsonObject rawJSON = ops.get(WithAlias.class.getName() + ":" + id1, JsonObject.class);
    System.out.println(rawJSON.toString());

    Optional<WithAlias> alsoMaybeDoc1 = repository.findFirstByNumber(42);
    assertTrue(alsoMaybeDoc1.isPresent());
    WithAlias alsoDoc1 = alsoMaybeDoc1.get();

    assertThat(alsoDoc1.getText()).isEqualTo(doc1.getText());
  }

}
