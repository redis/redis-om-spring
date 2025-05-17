package com.redis.om.spring.annotations.hash;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.Direccion;
import com.redis.om.spring.fixtures.hash.model.WithAlias;
import com.redis.om.spring.fixtures.hash.model.WithAlias$;
import com.redis.om.spring.fixtures.hash.repository.WithAliasRepository;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;

@SuppressWarnings(
  "SpellCheckingInspection"
)
class RedisHashWithAliasTest extends AbstractBaseEnhancedRedisTest {
  @Autowired
  WithAliasRepository repository;

  String id1;
  @SuppressWarnings(
    "unused"
  )
  String id2;

  @Autowired
  EntityStream entityStream;

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
    doc2 = repository.save(doc2);

    id2 = doc2.getId();
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

  @Test
  void testGetByAliasedProperty() {
    SearchStream<WithAlias> stream = entityStream.of(WithAlias.class);
    List<WithAlias> docs = stream //
        .filter(WithAlias$.TEXT.eq("Oye man")) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(docs).hasSize(1), () -> assertThat(docs).extracting("text").containsOnly("Oye man"));
  }

  @Test
  void testGetByTagAliasedProperty() {
    SearchStream<WithAlias> stream = entityStream.of(WithAlias.class);
    List<WithAlias> docs = stream //
        .filter(WithAlias$.TAGS.in("articulo", "article")) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(docs).hasSize(2), () -> assertThat(docs).extracting("text").containsOnly("Epa chamo",
            "Oye man"));
  }

}
