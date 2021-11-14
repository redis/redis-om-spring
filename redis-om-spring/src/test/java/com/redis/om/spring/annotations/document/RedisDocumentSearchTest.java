package com.redis.om.spring.annotations.document;

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
import org.springframework.data.redis.core.RedisTemplate;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.MyDoc;
import com.redis.om.spring.annotations.document.fixtures.MyDocRepository;

import io.redisearch.AggregationResult;
import io.redisearch.Document;
import io.redisearch.SearchResult;
import io.redisearch.aggregation.Row;

public class RedisDocumentSearchTest extends AbstractBaseDocumentTest {
  @Autowired MyDocRepository repository;

  @Autowired
  RedisTemplate<String, String> template;

  @BeforeEach
  public void loadTestData() {
    MyDoc doc1 = MyDoc.of("hello world");

    Set<String> tags = new HashSet<String>();
    tags.add("news");
    tags.add("article");

    doc1.setTag(tags);
    doc1 = repository.save(doc1);
  }

  @AfterEach
  public void cleanUp() {
    repository.deleteAll();
  }

  @Test
  public void testBasicCrudOperations() {
    assertEquals(1, repository.count());

    Set<String> keys = template.opsForSet().members(MyDoc.class.getName());


    String id = keys.iterator().next();

    Optional<MyDoc> maybeDoc1 = repository.findById(id);
    assertTrue(maybeDoc1.isPresent());

    assertEquals("hello world", maybeDoc1.get().getTitle());
  }

  @Test
  public void testCustomFinder() {
    Optional<MyDoc> maybeDoc1 = repository.findByTitle("hello world");
    assertTrue(maybeDoc1.isPresent());
  }

  /**
   * > FT.SEARCH idx * RETURN 3 $.tag[0] AS first_tag
   * 1) (integer) 1
   * 2) "doc1"
   * 3) 1) "first_tag"
   *    2) "news"
   *
   * @Query(returnFields = {"$.tag[0]", "AS", "first_tag"})
   * SearchResult getFirstTag();
   */
  @Test
  public void testQueryAnnotation01() {
    /*
     * @Query("* RETURN 3 $.tag[0] AS first_tag")
     * SearchResult getFirstTag();
     */
    SearchResult result = repository.getFirstTag();
    assertEquals(1, result.totalResults);
    Document doc = result.docs.get(0);
    assertEquals(1.0, doc.getScore(), 0);
    assertNull(doc.getPayload());
    assertTrue(StreamSupport //
        .stream(doc.getProperties().spliterator(), false) //
        .anyMatch(p -> p.getKey().contentEquals("first_tag") && p.getValue().equals("news")));
  }

  /**
   * @Query("@title:$title @tag:{$tags}")
   *
   * > FT.SEARCH idx '@title:hello @tag:{news}'
   * 1) (integer) 1 2) "doc1" 3) 1) "$"
   * 2) "{\"title\":\"hello world\",\"tag\":[\"news\",\"article\"]}"
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
   * @Aggregation(load = {"$.tag[1]", "AS", "tag2"})
   *
   * > FT.AGGREGATE idx * LOAD 3 $.tag[1] AS tag2
   *   1) (integer) 1
   *   2) 1) "tag2"
   *      2) "article"
   */
  @Test
  public void testAggregationAnnotation01() {
    AggregationResult result = repository.getSecondTagWithAggregation();
    assertEquals(1, result.totalResults);
    Row row = result.getRow(0);
    assertNotNull(row);
    assertTrue(row.containsKey("tag2"));
    assertEquals(row.getString("tag2"), "article");
  }
}
