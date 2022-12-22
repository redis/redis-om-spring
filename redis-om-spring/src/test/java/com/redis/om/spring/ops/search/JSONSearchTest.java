package com.redis.om.spring.ops.search;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.*;
import redis.clients.jedis.search.Schema.Field;
import redis.clients.jedis.search.Schema.FieldType;
import redis.clients.jedis.search.Schema.TextField;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.aggr.Row;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection") class JSONSearchTest extends AbstractBaseDocumentTest {
  public static final String searchIndex = "idx";

  /* A simple class that represents an object in real life */
  /* '{"title":"hello world", "tag": ["news", "article"]}' */
  private static class SomeJSON {
    @SuppressWarnings("unused")
    public String title;
    public final Set<String> tag = new HashSet<>();

    public SomeJSON() {
      this.title = "hello world";
      this.tag.add("news");
      this.tag.add("article");
    }
  }

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @Autowired
  private StringRedisTemplate template;

  @BeforeEach
  void setup() {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);

    try {
      ops.dropIndex();
    } catch (JedisDataException jdee) {
      // IGNORE: Unknown Index name
    }

    // FT.CREATE idx ON JSON SCHEMA $.title AS title TEXT $.tag[*] AS tag TAG
    Schema sc = new Schema() //
        .addField(new TextField(FieldName.of("$.title").as("title"))) //
        .addField(new Field(FieldName.of("$.tag[*]").as("tag"), FieldType.TAG));

    IndexDefinition def = new IndexDefinition(IndexDefinition.Type.JSON);
    ops.createIndex(sc, IndexOptions.defaultOptions().setDefinition(def));

    if (Boolean.FALSE.equals(template.hasKey("doc1"))) {
      JSONOperations<String> json = modulesOperations.opsForJSON();
      json.set("doc1", new SomeJSON());
    }
  }

  @AfterEach
  void cleanUp() {
    template.delete("doc1");
  }

  /**
   * > FT.SEARCH idx '@title:hello @tag:{news}'
   * 1) (integer) 1 2) "doc1" 3) 1) "$"
   * 2) "{\"title\":\"hello world\",\"tag\":[\"news\",\"article\"]}"
   */
  @Test
  void testBasicSearchOverJSON() {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);

    SearchResult result = ops.search(new Query("@title:hello @tag:{news}"));
    assertEquals(1, result.getTotalResults());
    Document doc = result.getDocuments().get(0);
    assertEquals(1.0, doc.getScore(), 0);
    assertNull(doc.getPayload());
    assertEquals("{\"title\":\"hello world\",\"tag\":[\"news\",\"article\"]}", doc.get("$"));
  }

  /**
   * > FT.SEARCH idx * RETURN 3 $.tag[0] AS first_tag
   * 1) (integer) 1
   * 2) "doc1"
   * 3) 1) "first_tag"
   * 2) "news"
   */
  @Test
  void testSearchOverJSONWithPathProjection() {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);
    SearchResult result = ops.search(new Query("*").returnFields("$.tag[0]", "AS", "first_tag"));
    assertEquals(1, result.getTotalResults());
    Document doc = result.getDocuments().get(0);
    assertEquals(1.0, doc.getScore(), 0);
    assertNull(doc.getPayload());
    assertTrue(StreamSupport //
        .stream(doc.getProperties().spliterator(), false) //
        .anyMatch(p -> p.getKey().contentEquals("first_tag") && p.getValue().equals("news")));
  }

  /**
   * > FT.AGGREGATE idx * LOAD 3 $.tag[1] AS tag2
   * 1) (integer) 1
   * 2) 1) "tag2"
   * 2) "article"
   */
  @Test
  void testAggregateLoadUsingJSONPath() {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);

    AggregationBuilder aggregation = new AggregationBuilder().load("$.tag[1]", "AS", "tag2");

    // actual search
    AggregationResult result = ops.aggregate(aggregation);
    assertEquals(1, result.getTotalResults());
    Row row = result.getRow(0);
    assertNotNull(row);
    assertTrue(row.containsKey("tag2"));
    assertEquals("article", row.getString("tag2"));
  }
}
