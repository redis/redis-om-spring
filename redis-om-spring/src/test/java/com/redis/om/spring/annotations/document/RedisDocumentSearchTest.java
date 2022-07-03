package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.MyDoc;
import com.redis.om.spring.annotations.document.fixtures.MyDocRepository;
import com.redislabs.modules.rejson.Path;

import io.redisearch.AggregationResult;
import io.redisearch.Document;
import io.redisearch.SearchResult;
import io.redisearch.aggregation.Row;

public class RedisDocumentSearchTest extends AbstractBaseDocumentTest {
  @Autowired
  MyDocRepository repository;

  @Autowired
  RedisTemplate<String, String> template;

  String id1;
  String id2;

  @BeforeEach
  public void loadTestData() {
    MyDoc doc1 = MyDoc.of("hello world");

    Set<String> tags = new HashSet<String>();
    tags.add("news");
    tags.add("article");

    doc1.setTag(tags);
    doc1 = repository.save(doc1);

    id1 = doc1.getId();

    MyDoc doc2 = MyDoc.of("hello mundo");

    Set<String> tags2 = new HashSet<String>();
    tags2.add("noticias");
    tags2.add("articulo");

    doc2.setTag(tags2);
    doc2 = repository.save(doc2);

    id2 = doc2.getId();
  }

  @AfterEach
  public void cleanUp() {
    repository.deleteAll();
  }

  @Test
  public void testBasicCrudOperations() {
    assertEquals(2, repository.count());

    Optional<MyDoc> maybeDoc1 = repository.findById(id1);
    assertTrue(maybeDoc1.isPresent());

    assertEquals("hello world", maybeDoc1.get().getTitle());
  }

  @Test
  public void testCustomFinder() {
    Optional<MyDoc> maybeDoc1 = repository.findByTitle("hello world");
    assertTrue(maybeDoc1.isPresent());
  }

  /**
   * <pre>
   * > FT.SEARCH idx * RETURN 3 $.tag[0] AS first_tag 
   * 1) (integer) 1 
   * 2) "doc1" 
   * 3) 1) "first_tag" 
   *    2) "news"
   *
   * @Query(returnFields = {"$.tag[0]", "AS", "first_tag"}) 
   * SearchResult getFirstTag();
   * </pre>
   */
  @Test
  public void testQueryAnnotation01() {
    SearchResult result = repository.getFirstTag();
    assertEquals(2, result.totalResults);
    Document doc = result.docs.get(0);
    assertEquals(1.0, doc.getScore(), 0);
    assertNull(doc.getPayload());
    assertTrue(StreamSupport //
        .stream(doc.getProperties().spliterator(), false) //
        .anyMatch(p -> p.getKey().contentEquals("first_tag") && p.getValue().equals("news")));
  }

  /**
   * <pre>
   * @Query("@title:$title @tag:{$tags}")
   *
   * > FT.SEARCH idx '@title:hello @tag:{news}' 
   * 1) (integer) 1 2) "doc1" 
   * 3) 1) "$"
   *    2) "{\"title\":\"hello world\",\"tag\":[\"news\",\"article\"]}"
   * </pre>
   */
  @Test
  public void testQueryAnnotation02() {
    Iterable<MyDoc> results = repository.findByTitleAndTags("hello", Set.of("news"));
    assertEquals(1, ((Collection<MyDoc>) results).size());
    MyDoc doc = results.iterator().next();
    assertEquals("hello world", doc.getTitle());
    assertTrue(doc.getTag().contains("news"));
  }

  /**
   * <pre>
   * @Aggregation(load = {"$.tag[1]", "AS", "tag2"})
   *
   * > FT.AGGREGATE idx * LOAD 3 $.tag[1] AS tag2 1) (integer) 1
   * 2) 1) "tag2" 
   *    2) "article"
   * </pre>
   */
  @Test
  public void testAggregationAnnotation01() {
    AggregationResult result = repository.getSecondTagWithAggregation();
    assertEquals(1, result.totalResults);
    Row row = result.getRow(0);
    assertNotNull(row);
    assertTrue(row.containsKey("tag2"));
    assertEquals("article", row.getString("tag2"));
  }

  @Test
  public void testBasicPagination() {
    Pageable pageRequest = PageRequest.of(0, 1);

    Page<MyDoc> result = repository.findAllByTitleStartingWith("hel", pageRequest);

    assertEquals(2, result.getTotalPages());
    assertEquals(2, result.getTotalElements());
    assertEquals(1, result.getContent().size());

    MyDoc doc1 = result.getContent().get(0);
    assertEquals("hello world", doc1.getTitle());
  }

  @Test
  public void testQueryAnnotationWithLimitAndOffset() {
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
  public void testBasicPagination2() {
    Pageable pageRequest = PageRequest.of(1, 1);

    Page<MyDoc> result = repository.findAllByTitleStartingWith("hel", pageRequest);

    assertEquals(2, result.getTotalPages());
    assertEquals(2, result.getTotalElements());
    assertEquals(1, result.getContent().size());

    MyDoc doc1 = result.getContent().get(0);
    assertEquals("hello mundo", doc1.getTitle());
  }

  @Test
  public void testBasicPaginationWithSorting() {
    Pageable pageRequest = PageRequest.of(0, 2, Sort.by("title").ascending());

    Page<MyDoc> result = repository.findAllByTitleStartingWith("hel", pageRequest);

    assertEquals(1, result.getTotalPages());
    assertEquals(2, result.getTotalElements());
    assertEquals(2, result.getContent().size());

    assertEquals("hello mundo", result.getContent().get(0).getTitle());
    assertEquals("hello world", result.getContent().get(1).getTitle());
  }

  @Test
  public void testTagValsQuery() {
    Iterable<String> values = repository.getAllTags();
    assertThat(values).hasSize(4).contains("news", "article", "noticias", "articulo");
  }

  @Test
  public void testGetIds() {
    Iterable<String> ids = repository.getIds();
    assertThat(ids).hasSize(2).contains(id1, id2);
  }

  @Test
  public void testGetIdsWithPaging() {
    Pageable pageRequest = PageRequest.of(0, 1);

    Page<String> ids = repository.getIds(pageRequest);
    assertThat(ids).hasSize(1);

    ids = repository.getIds(pageRequest.next());
    assertThat(ids).hasSize(1);
  }

  @Test
  public void testDeleteByIdWithPath() {
    repository.deleteById(id1, Path.of("$.tag"));

    Optional<MyDoc> maybeDoc1 = repository.findById(id1);
    assertTrue(maybeDoc1.isPresent());

    MyDoc doc1 = maybeDoc1.get();

    assertEquals("hello world", doc1.getTitle());

    assertThat(doc1.getTag()).isEmpty();
  }

}
