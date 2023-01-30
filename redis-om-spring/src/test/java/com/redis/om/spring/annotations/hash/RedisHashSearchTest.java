package com.redis.om.spring.annotations.hash;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.hash.fixtures.MyHash;
import com.redis.om.spring.annotations.hash.fixtures.MyHashRepository;
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
import redis.clients.jedis.search.SearchResult;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpellCheckingInspection") class RedisHashSearchTest extends AbstractBaseEnhancedRedisTest {
  @Autowired
  MyHashRepository repository;

  @Autowired StringRedisTemplate template;

  String id1;
  String id2;

  @BeforeEach
  void loadTestData() {
    Point point1 = new Point(-122.124500, 47.640160);
    MyHash doc1 = MyHash.of("hello world", point1, point1, 1);

    Set<String> tags = new HashSet<>();
    tags.add("news");
    tags.add("article");

    doc1.setTag(tags);
    doc1 = repository.save(doc1);

    id1 = doc1.getId();

    Point point2 = new Point(-122.066540, 37.377690);
    MyHash doc2 = MyHash.of("hello mundo", point2, point2, 2);

    Set<String> tags2 = new HashSet<>();
    tags2.add("noticias");
    tags2.add("articulo");

    doc2.setTag(tags2);
    doc2 = repository.save(doc2);

    id2 = doc2.getId();
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
    flushSearchIndexFor(MyHash.class);
  }

  @Test
  void testBasicCrudOperations() {
    assertEquals(2, repository.count());

    Optional<MyHash> maybeDoc1 = repository.findById(id1);
    assertTrue(maybeDoc1.isPresent());

    assertEquals("hello world", maybeDoc1.get().getTitle());
  }

  @Test
  void testCustomFinder() {
    Optional<MyHash> maybeDoc1 = repository.findByTitle("hello world");
    assertTrue(maybeDoc1.isPresent());
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
    Iterable<MyHash> results = repository.findByTitleAndTags("hello", Set.of("news"));
    assertEquals(1, ((Collection<MyHash>) results).size());
    MyHash doc = results.iterator().next();
    assertEquals("hello world", doc.getTitle());
    assertTrue(doc.getTag().contains("news"));
  }

  @Test
  void testBasicPagination() {
    Pageable pageRequest = PageRequest.of(0, 1);

    Page<MyHash> result = repository.findAllByTitleStartingWith("hel", pageRequest);

    assertEquals(2, result.getTotalPages());
    assertEquals(2, result.getTotalElements());
    assertEquals(1, result.getContent().size());

    MyHash doc1 = result.getContent().get(0);
    assertEquals("hello world", doc1.getTitle());
  }

  @Test
  void testUnpagedPagination() {
    Page<MyHash> result = repository.findAllByTitleStartingWith("hel", Pageable.unpaged());

    assertEquals(1, result.getTotalPages());
    assertEquals(2, result.getTotalElements());
    assertEquals(2, result.getContent().size());

    MyHash doc1 = result.getContent().get(0);
    assertEquals("hello world", doc1.getTitle());
  }

  @Test
  void testQueryAnnotationWithLimitAndOffset() {
    Pageable pageRequest = PageRequest.of(0, 1);
    Page<MyHash> result = repository.customFindAllByTitleStartingWith("hel", pageRequest);

    assertThat(result).hasSize(1);
    assertEquals(2, result.getTotalPages());
    assertEquals(2, result.getTotalElements());
    assertEquals(1, result.getContent().size());

    MyHash doc1 = result.getContent().get(0);
    assertEquals("hello world", doc1.getTitle());
  }

  @Test
  void testBasicPagination2() {
    Pageable pageRequest = PageRequest.of(1, 1);

    Page<MyHash> result = repository.findAllByTitleStartingWith("hel", pageRequest);

    assertEquals(2, result.getTotalPages());
    assertEquals(2, result.getTotalElements());
    assertEquals(1, result.getContent().size());

    MyHash doc1 = result.getContent().get(0);
    assertEquals("hello mundo", doc1.getTitle());
  }

  @Test
  void testBasicPaginationWithSorting() {
    Pageable pageRequest = PageRequest.of(0, 2, Sort.by("title").ascending());

    Page<MyHash> result = repository.findAllByTitleStartingWith("hel", pageRequest);

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
    Iterable<MyHash> results = repository.findByTag(Set.of("news"));
    assertEquals(1, ((Collection<MyHash>) results).size());
    MyHash doc = results.iterator().next();
    assertEquals("hello world", doc.getTitle());
    assertTrue(doc.getTag().contains("news"));
  }

  @Test
  void testFindByFieldWithExplicitGeoIndexedAnnotation() {
    Point point = new Point(-122.064, 37.384);
    var distance = new Distance(30.0, DistanceUnit.MILES);
    Iterable<MyHash> results = repository.findByLocationNear(point, distance);
    Iterator<MyHash> iter = results.iterator();
    MyHash doc = iter.next();
    assertEquals("hello mundo", doc.getTitle());

    @SuppressWarnings("unused")
    NoSuchElementException exception = Assertions.assertThrows(NoSuchElementException.class, iter::next);
  }

  @Test
  void testFindByFieldWithIndexedGeoAnnotation() {
    Point point = new Point(-122.064, 37.384);
    var distance = new Distance(30.0, DistanceUnit.MILES);
    Iterable<MyHash> results = repository.findByLocation2Near(point, distance);
    Iterator<MyHash> iter = results.iterator();
    MyHash doc = iter.next();
    assertEquals("hello mundo", doc.getTitle());

    @SuppressWarnings("unused")
    NoSuchElementException exception = Assertions.assertThrows(NoSuchElementException.class, iter::next);
  }

  @Test
  void testFindByFieldWithExplicitNumericIndexedAnnotation() {
    Iterable<MyHash> results = repository.findByaNumber(1);
    assertEquals(1, ((Collection<MyHash>) results).size());
    MyHash doc = results.iterator().next();
    assertEquals("hello world", doc.getTitle());
    assertThat(doc.getTag()).containsExactlyInAnyOrder("news", "article");
  }
  
  @Test
  void testQueryAnnotationWithReturnFieldsAndLimitAndOffset() {
    repository.deleteAll();
    Point point = new Point(-122.066540, 37.377690);
    repository.saveAll(List.of(
        MyHash.of("predisposition", point, point, 4), //
        MyHash.of("predestination", point, point, 8), //
        MyHash.of("prepublication", point, point, 15), //
        MyHash.of("predestinarian", point, point, 16), //
        MyHash.of("preadolescence", point, point, 23), //
        MyHash.of("premillenarian", point, point, 42), //
        MyHash.of("precipitinogen", point, point, 4), //
        MyHash.of("precipitations", point, point, 8), //
        MyHash.of("precociousness", point, point, 15), //
        MyHash.of("precombustions", point, point, 16), //
        MyHash.of("preconditioned", point, point, 23), //
        MyHash.of("preconceptions", point, point, 42), //
        MyHash.of("precipitancies", point, point, 4), //
        MyHash.of("preciousnesses", point, point, 8), //
        MyHash.of("precentorships", point, point, 15), //
        MyHash.of("preceptorships", point, point, 16) //
    ));

    SearchResult result = repository.customFindAllByTitleStartingWithReturnFieldsAndLimit("pre");

    assertThat(result.getTotalResults()).isEqualTo(16);
    assertThat(result.getDocuments()).hasSize(12);
    assertThat(result.getDocuments().get(0).get("title")).isEqualTo("precentorships");
    assertThat(result.getDocuments().get(1).get("title")).isEqualTo("preceptorships");
    assertThat(result.getDocuments().get(2).get("title")).isEqualTo("preciousnesses");
    assertThat(result.getDocuments().get(3).get("title")).isEqualTo("precipitancies");
    assertThat(result.getDocuments().get(4).get("title")).isEqualTo("precipitations");
    assertThat(result.getDocuments().get(5).get("title")).isEqualTo("precipitinogen");
    assertThat(result.getDocuments().get(6).get("title")).isEqualTo("precociousness");
    assertThat(result.getDocuments().get(7).get("title")).isEqualTo("precombustions");
    assertThat(result.getDocuments().get(8).get("title")).isEqualTo("preconceptions");
    assertThat(result.getDocuments().get(9).get("title")).isEqualTo("preconditioned");
    assertThat(result.getDocuments().get(10).get("title")).isEqualTo("predestinarian");
    assertThat(result.getDocuments().get(11).get("title")).isEqualTo("predestination");
  }

}
