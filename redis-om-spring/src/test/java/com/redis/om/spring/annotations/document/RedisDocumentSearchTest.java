package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.MyDoc;
import com.redis.om.spring.annotations.document.fixtures.MyDocRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.aggr.Row;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection") class RedisDocumentSearchTest extends AbstractBaseDocumentTest {
  @Autowired
  MyDocRepository repository;

  @Autowired StringRedisTemplate template;

  private static String id1;
  private static String id2;

  @BeforeEach
  void loadTestData() {
    Point point1 = new Point(-122.124500, 47.640160);
    MyDoc doc1 = MyDoc.of("hello world", point1, point1, 1);

    doc1.setTag(Set.of("news", "article"));
    doc1 = repository.save(doc1);
    id1 = doc1.getId();

    Point point2 = new Point(-122.066540, 37.377690);
    MyDoc doc2 = MyDoc.of("hello mundo", point2, point2, 2);

    doc2.setTag(Set.of("noticias", "articulo"));
    doc2 = repository.save(doc2);
    id2 = doc2.getId();
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void testBasicCrudOperations() {
    assertEquals(2, repository.count());

    Optional<MyDoc> maybeDoc1 = repository.findById(id1);
    assertThat(maybeDoc1).isPresent().map(MyDoc::getTitle).contains("hello world");
  }

  @Test
  void testCustomFinder() {
    Optional<MyDoc> maybeDoc1 = repository.findByTitle("hello world");
    assertThat(maybeDoc1).isPresent();
  }

  /**
   * <pre>
   * > FT.SEARCH idx * RETURN 3 $.tag[0] AS first_tag
   * 1) (integer) 1
   * 2) "doc1"
   * 3) 1) "first_tag"
   *    2) "news"
   * </pre>
   * <pre>{@code
   * @Query(returnFields = {"$.tag[0]", "AS", "first_tag"})
   * SearchResult getFirstTag();
   * }</pre>
   */
  @Test
  void testQueryAnnotation01() {
    SearchResult result = repository.getFirstTag();

    assertAll( //
        () -> assertThat(result.getTotalResults()).isEqualTo(2),
        () -> assertThat(result.getDocuments().get(0).getScore()).isEqualTo(1.0),
        () -> assertThat(result.getDocuments().get(0).getString("first_tag")).isIn("news", "article")
    );
  }

  /**
   * <pre>
   * &#64;Query("@title:$title @tag:{$tags}")
   *
   * > FT.SEARCH idx '@title:hello @tag:{news}'
   * 1) (integer) 1 2) "doc1"
   * 3) 1) "$"
   *    2) "{\"title\":\"hello world\",\"tag\":[\"news\",\"article\"]}"
   * </pre>
   */
  @Test
  void testQueryAnnotation02() {
    Iterable<MyDoc> results = repository.findByTitleAndTags("hello", Set.of("news"));
    assertEquals(1, ((Collection<MyDoc>) results).size());
    MyDoc doc = results.iterator().next();
    assertAll( //
        () -> assertThat(doc.getTitle()).isEqualTo("hello world"),
        () -> assertThat(doc.getTag()).contains("news")
    );
  }

  /**
   * <pre>{@code
   * @Aggregation(load = {"$.tag[1]", "AS", "tag2"})
   * }</pre>
   * <pre>
   * > FT.AGGREGATE idx * LOAD 3 $.tag[1] AS tag2 1) (integer) 1
   * 2) 1) "tag2"
   *    2) "article"
   * </pre>
   */
  @Test
  void testAggregationAnnotation01() {
    AggregationResult result = repository.getSecondTagWithAggregation();
    assertEquals(1, result.getTotalResults());
    Row row = result.getRow(0);

    assertAll( //
        () -> assertThat(row).isNotNull(),
        () -> assertThat(row.getString("tag2")).isIn("news", "article")
    );
  }

  @Test
  void testBasicPagination() {
    Pageable pageRequest = PageRequest.of(0, 1);

    Page<MyDoc> result = repository.findAllByTitleStartingWith("hel", pageRequest);

    assertAll( //
        () -> assertEquals(2, result.getTotalPages()),
        () -> assertEquals(2, result.getTotalElements()),
        () -> assertEquals(1, result.getContent().size()),
        () -> assertEquals("hello world", result.getContent().get(0).getTitle())
    );
  }

  @Test
  void testUnpagedPagination() {
    Page<MyDoc> result = repository.findAllByTitleStartingWith("hel", Pageable.unpaged());

    assertEquals(1, result.getTotalPages());
    assertEquals(2, result.getTotalElements());
    assertEquals(2, result.getContent().size());

    MyDoc doc1 = result.getContent().get(0);
    assertEquals("hello world", doc1.getTitle());
  }

  @Test
  void testQueryAnnotationWithLimitAndOffset() {
    Pageable pageRequest = PageRequest.of(0, 1);
    Page<MyDoc> result = repository.customFindAllByTitleStartingWith("hel", pageRequest);

    assertThat(result).hasSize(1);
    assertEquals(2, result.getTotalPages());
    assertEquals(2, result.getTotalElements());
    assertEquals(1, result.getContent().size());

    MyDoc doc1 = result.getContent().get(0);
    assertEquals("hello world", doc1.getTitle());
  }

  @Test
  void testBasicPagination2() {
    Pageable pageRequest = PageRequest.of(1, 1);

    Page<MyDoc> result = repository.findAllByTitleStartingWith("hel", pageRequest);

    assertEquals(2, result.getTotalPages());
    assertEquals(2, result.getTotalElements());
    assertEquals(1, result.getContent().size());

    MyDoc doc1 = result.getContent().get(0);
    assertEquals("hello mundo", doc1.getTitle());
  }

  @Test
  void testBasicPaginationWithSorting() {
    Pageable pageRequest = PageRequest.of(0, 2, Sort.by("title").ascending());

    Page<MyDoc> result = repository.findAllByTitleStartingWith("hel", pageRequest);

    assertEquals(1, result.getTotalPages());
    assertEquals(2, result.getTotalElements());
    assertEquals(2, result.getContent().size());

    assertEquals("hello mundo", result.getContent().get(0).getTitle());
    assertEquals("hello world", result.getContent().get(1).getTitle());
  }

  @Test
  void testTagValsQuery() {
    Iterable<String> values = repository.getAllTag();
    assertThat(values).hasSize(4).contains("news", "article", "noticias", "articulo");
  }

  @Test
  void testGetIds() {
    Iterable<String> ids = repository.getIds();
    assertThat(ids).hasSize(2).contains(id1, id2);
  }

  @Test
  void testGetIdsWithPaging() {
    Pageable pageRequest = PageRequest.of(0, 1);

    Page<String> ids = repository.getIds(pageRequest);
    assertThat(ids).hasSize(1);

    ids = repository.getIds(pageRequest.next());
    assertThat(ids).hasSize(1);
  }

  @Test
  void testFindByFieldWithExplicitTagIndexedAnnotation() {
    Iterable<MyDoc> results = repository.findByTag(Set.of("news"));
    assertEquals(1, ((Collection<MyDoc>) results).size());
    MyDoc doc = results.iterator().next();
    assertEquals("hello world", doc.getTitle());
    assertTrue(doc.getTag().contains("news"));
  }

  @Test
  void testFindByFieldWithExplicitGeoIndexedAnnotation() {
    Point point = new Point(-122.064, 37.384);
    var distance = new Distance(30.0, DistanceUnit.MILES);
    Iterable<MyDoc> results = repository.findByLocationNear(point, distance);
    Iterator<MyDoc> iter = results.iterator();
    MyDoc doc = iter.next();
    assertEquals("hello mundo", doc.getTitle());

    @SuppressWarnings("unused")
    NoSuchElementException exception = Assertions.assertThrows(NoSuchElementException.class, iter::next);
  }

  @Test
  void testFindByFieldWithIndexedGeoAnnotation() {
    Point point = new Point(-122.064, 37.384);
    var distance = new Distance(30.0, DistanceUnit.MILES);
    Iterable<MyDoc> results = repository.findByLocation2Near(point, distance);
    Iterator<MyDoc> iter = results.iterator();
    MyDoc doc = iter.next();
    assertEquals("hello mundo", doc.getTitle());

    @SuppressWarnings("unused")
    NoSuchElementException exception = Assertions.assertThrows(NoSuchElementException.class, iter::next);
  }

  @Test
  void testFindByFieldWithExplicitNumericIndexedAnnotation() {
    Iterable<MyDoc> results = repository.findByaNumber(1);
    assertEquals(1, ((Collection<MyDoc>) results).size());
    MyDoc doc = results.iterator().next();
    assertEquals("hello world", doc.getTitle());
    assertTrue(doc.getTag().contains("news"));
  }

  @Test
  void testDeleteByIdWithPath() {
    repository.deleteById(id1, Path.of("$.tag"));

    Optional<MyDoc> maybeDoc1 = repository.findById(id1);
    assertTrue(maybeDoc1.isPresent());

    MyDoc doc1 = maybeDoc1.get();

    assertEquals("hello world", doc1.getTitle());

    assertThat(doc1.getTag()).isEmpty();
  }

  @Test
  void testQueryAnnotationWithReturnFieldsAndLimitAndOffset() {
    Point point = new Point(-122.066540, 37.377690);
    repository.saveAll(List.of(
        MyDoc.of("predisposition", point, point, 4), //
        MyDoc.of("predestination", point, point, 8), //
        MyDoc.of("prepublication", point, point, 15), //
        MyDoc.of("predestinarian", point, point, 16), //
        MyDoc.of("preadolescence", point, point, 23), //
        MyDoc.of("premillenarian", point, point, 42), //
        MyDoc.of("precipitinogen", point, point, 4), //
        MyDoc.of("precipitations", point, point, 8), //
        MyDoc.of("precociousness", point, point, 15), //
        MyDoc.of("precombustions", point, point, 16), //
        MyDoc.of("preconditioned", point, point, 23), //
        MyDoc.of("preconceptions", point, point, 42), //
        MyDoc.of("precipitancies", point, point, 4), //
        MyDoc.of("preciousnesses", point, point, 8), //
        MyDoc.of("precentorships", point, point, 15), //
        MyDoc.of("preceptorships", point, point, 16) //
    ));

    SearchResult result = repository.customFindAllByTitleStartingWithReturnFieldsAndLimit("pre");
    assertEquals(16, result.getTotalResults());
    assertThat(result.getTotalResults()).isEqualTo(16);

    List<String> titles = result.getDocuments().stream().map(d -> d.get("title")).map(Object::toString).toList();

    assertThat(titles).containsExactly("precentorships", "preceptorships", "preciousnesses", "precipitancies",
        "precipitations", "precipitinogen", "precociousness", "precombustions", "preconceptions", "preconditioned",
        "predestinarian", "predestination");
  }

}
