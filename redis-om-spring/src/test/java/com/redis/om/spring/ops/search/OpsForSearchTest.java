package com.redis.om.spring.ops.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.RedisModulesOperations;

import io.redisearch.Document;
import io.redisearch.Query;
import io.redisearch.Schema;
import io.redisearch.Schema.TagField;
import io.redisearch.Schema.TextField;
import io.redisearch.SearchResult;
import io.redisearch.client.AddOptions;
import io.redisearch.client.Client;
import io.redisearch.client.Client.IndexOptions;
import io.redisearch.client.ConfigOption;
import io.redisearch.client.IndexDefinition;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.util.SafeEncoder;

class OpsForSearchTest extends AbstractBaseDocumentTest {
  public static String searchIndex = "student_pupil";
  static String HASH_PREFIX = "Article";

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @Autowired
  RedisTemplate<String, String> template;

  @BeforeEach
  void setup() {
    HashOperations<String, String, String> hashOps = template.opsForHash();
    hashOps.putAll("profesor:5555", toMap("first", "Albert", "last", "Blue", "age", "55"));
    hashOps.putAll("student:1111", toMap("first", "Joe", "last", "Dod", "age", "18"));
    hashOps.putAll("pupil:2222", toMap("first", "Jen", "last", "Rod", "age", "14"));
    hashOps.putAll("student:3333", toMap("first", "El", "last", "Mark", "age", "17"));
    hashOps.putAll("pupil:4444", toMap("first", "Pat", "last", "Shu", "age", "21"));
    hashOps.putAll("student:5555", toMap("first", "Joen", "last", "Ko", "age", "20"));
    hashOps.putAll("teacher:6666", toMap("first", "Pat", "last", "Rod", "age", "20"));

    Schema sc = new Schema() //
        .addTextField("first", 1.0) //
        .addTextField("last", 1.0) //
        .addNumericField("age");

    IndexDefinition def = new IndexDefinition().setPrefixes(new String[] { "student:", "pupil:" });

    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);

    try {
      ops.createIndex(sc, Client.IndexOptions.defaultOptions().setDefinition(def));
    } catch (JedisDataException jdee) {
      // IGNORE: Unknown Index name
    }
  }

  @AfterEach
  void cleanUp() {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);
    try {
      ops.dropIndex();
    } catch (JedisDataException jdee) {
      // IGNORE: Unknown Index name
    }

    template.delete("profesor:5555");
    template.delete("student:1111");
    template.delete("pupil:2222");
    template.delete("student:3333");
    template.delete("pupil:4444");
    template.delete("student:5555");
    template.delete("teacher:6666");
  }

  @Test
  void testBasicSearch() {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);
    SearchResult res1 = ops.search(new Query("@first:Jo*"));
    assertEquals(2, res1.totalResults);

    SearchResult res2 = ops.search(new Query("@first:Pat"));
    assertEquals(1, res2.totalResults);
  }

  @Test
  void testComplexSearch() throws Exception {
    SearchOperations<String> ops = modulesOperations.opsForSearch("testComplexSearch");

    Schema sc = new Schema().addTextField("title", 1.0).addTextField("body", 1.0);

    IndexDefinition index = new IndexDefinition();
    index.setPrefixes("testComplexSearch:");
    IndexOptions options = Client.IndexOptions.defaultOptions().setDefinition(index);
    assertTrue(ops.createIndex(sc, options));

    Map<String, Object> fields = new HashMap<>();
    fields.put("title", "hello world");
    fields.put("body", "lorem ipsum");
    for (int i = 0; i < 100; i++) {
      assertTrue(ops.addDocument(String.format("testComplexSearch:doc%d", i), (double) i / 100.0, fields));
    }

    SearchResult res = ops.search(new Query("hello world").limit(0, 5).setWithScores());
    assertEquals(100, res.totalResults);
    assertEquals(5, res.docs.size());
    for (Document d : res.docs) {
      assertTrue(d.getId().startsWith("testComplexSearch:doc"));
      assertTrue(d.getScore() < 100);
      assertEquals(String.format(
          "{\"id\":\"%s\",\"score\":%s,\"properties\":{\"title\":\"hello world\",\"body\":\"lorem ipsum\"}}", d.getId(),
          Double.toString(d.getScore())), d.toString());
    }

    assertTrue(ops.deleteDocument("testComplexSearch:doc0", true));
    assertFalse(ops.deleteDocument("testComplexSearch:doc0"));

    res = ops.search(new Query("hello world"));
    assertEquals(99, res.totalResults);

    assertTrue(ops.dropIndex());
    boolean threw = false;
    try {
      res = ops.search(new Query("hello world"));

    } catch (Exception e) {
      threw = true;
    }
    assertTrue(threw);
  }

  @Test
  void searchBatch() throws Exception {
    SearchOperations<String> ops = modulesOperations.opsForSearch("batch");

    Schema sc = new Schema().addTextField("title", 1.0).addTextField("body", 1.0);

    assertTrue(ops.createIndex(sc, Client.IndexOptions.defaultOptions()));
    Map<String, Object> fields = new HashMap<>();
    fields.put("title", "hello world");
    fields.put("body", "lorem ipsum");
    for (int i = 0; i < 50; i++) {
      fields.put("title", "hello world");
      assertTrue(ops.addDocument(String.format("doc%d", i), (double) i / 100.0, fields));
    }

    for (int i = 50; i < 100; i++) {
      fields.put("title", "good night");
      assertTrue(ops.addDocument(String.format("doc%d", i), (double) i / 100.0, fields));
    }

    SearchResult[] res = ops.searchBatch(new Query("hello world").limit(0, 5).setWithScores(),
        new Query("good night").limit(0, 5).setWithScores());

    assertEquals(2, res.length);
    assertEquals(50, res[0].totalResults);
    assertEquals(50, res[1].totalResults);
    assertEquals(5, res[0].docs.size());
    for (Document d : res[0].docs) {
      assertTrue(d.getId().startsWith("doc"));
      assertTrue(d.getScore() < 100);
      assertEquals(String.format(
          "{\"id\":\"%s\",\"score\":%s,\"properties\":{\"title\":\"hello world\",\"body\":\"lorem ipsum\"}}", d.getId(),
          Double.toString(d.getScore())), d.toString());
    }
    dropIndex("batch");
  }

  @Test
  void testExplain() {
    SearchOperations<String> ops = modulesOperations.opsForSearch("explain");

    Schema sc = new Schema().addTextField("f1", 1.0).addTextField("f2", 1.0).addTextField("f3", 1.0);
    ops.createIndex(sc, Client.IndexOptions.defaultOptions());

    String res = ops.explain(new Query("@f3:f3_val @f2:f2_val @f1:f1_val"));
    assertNotNull(res);
    assertFalse(res.isEmpty());
    dropIndex("explain");
  }

  @Test
  void testLanguage() throws Exception {
    SearchOperations<String> ops = modulesOperations.opsForSearch("language");
    Schema sc = new Schema().addTextField("text", 1.0);
    ops.createIndex(sc, Client.IndexOptions.defaultOptions());

    Document d = new Document("doc1").set("text", "hello");
    AddOptions options = new AddOptions().setLanguage("spanish");
    assertTrue(ops.addDocument(d, options));
    boolean caught = false;

    options.setLanguage("ybreski");
    ops.deleteDocument(d.getId());

    try {
      ops.addDocument(d, options);
    } catch (JedisDataException t) {
      caught = true;
    }
    assertTrue(caught);
    dropIndex("language");
  }

  @Test
  void testMultiDocuments() {
    SearchOperations<String> ops = modulesOperations.opsForSearch("multi");
    Schema sc = new Schema().addTextField("title", 1.0).addTextField("body", 1.0);

    assertTrue(ops.createIndex(sc, Client.IndexOptions.defaultOptions()));

    Map<String, Object> fields = new HashMap<>();
    fields.put("title", "hello world");
    fields.put("body", "lorem ipsum");

    boolean[] results = ops.addDocuments(new Document("doc1", fields), new Document("doc2", fields),
        new Document("doc3", fields));

    assertArrayEquals(new boolean[] { true, true, true }, results);

    assertEquals(3, ops.search(new Query("hello world")).totalResults);

    results = ops.addDocuments(new Document("doc4", fields), new Document("doc2", fields),
        new Document("doc5", fields));
    assertArrayEquals(new boolean[] { true, false, true }, results);

    results = ops.deleteDocuments(true, "doc1", "doc2", "doc36");
    assertArrayEquals(new boolean[] { true, true, false }, results);
    dropIndex("multi");
  }

  @Test
  void testReplacePartial() throws Exception {
    SearchOperations<String> ops = modulesOperations.opsForSearch("replace_partial");

    Schema sc = new Schema().addTextField("f1", 1.0).addTextField("f2", 1.0).addTextField("f3", 1.0);
    assertTrue(ops.createIndex(sc, Client.IndexOptions.defaultOptions()));

    Map<String, Object> mm = new HashMap<>();
    mm.put("f1", "f1_val");
    mm.put("f2", "f2_val");

    assertTrue(ops.addDocument("doc1", mm));
    assertTrue(ops.addDocument("doc2", mm));

    mm.clear();
    mm.put("f3", "f3_val");

    assertTrue(ops.updateDocument("doc1", 1.0, mm));
    assertTrue(ops.replaceDocument("doc2", 1.0, mm));

    // Search for f3 value. All documents should have it.
    SearchResult res = ops.search(new Query(("@f3:f3_Val")));
    assertEquals(2, res.totalResults);

    res = ops.search(new Query("@f3:f3_val @f2:f2_val @f1:f1_val"));
    assertEquals(1, res.totalResults);
    dropIndex("replace_partial");
  }

  @Test
  void testInfo() throws Exception {
    SearchOperations<String> ops = modulesOperations.opsForSearch("movies");

    String TITLE = "title";
    String GENRE = "genre";
    String VOTES = "votes";
    String RATING = "rating";
    String RELEASE_YEAR = "release_year";
    String PLOT = "plot";

    Schema sc = new Schema().addTextField(TITLE, 5.0).addSortableTextField(PLOT, 1.0).addSortableTagField(GENRE, ",")
        .addSortableNumericField(RELEASE_YEAR).addSortableNumericField(RATING).addSortableNumericField(VOTES);

    assertTrue(ops.createIndex(sc, Client.IndexOptions.defaultOptions()));

    Map<String, Object> info = ops.getInfo();
    assertEquals("movies", info.get("index_name"));

    assertEquals(6, ((List<?>) info.get("attributes")).size());
    assertEquals("global_idle", ((List<?>) info.get("cursor_stats")).get(0));
    assertEquals(0L, ((List<?>) info.get("cursor_stats")).get(1));
    dropIndex("movies");
  }

  @Test
  void testGet() throws Exception {
    SearchOperations<String> ops = modulesOperations.opsForSearch("testGet");
    ops.createIndex(new Schema().addTextField("txt1", 1.0), Client.IndexOptions.defaultOptions());
    ops.addDocument(new Document("doc1").set("txt1", "Hello World!"), new AddOptions());
    Document d = ops.getDocument("doc1");
    assertNotNull(d);
    assertEquals("Hello World!", d.get("txt1"));

    // Get something that does not exist. Shouldn't explode
    assertNull(ops.getDocument("nonexist"));

    // Test decode=false mode
    d = ops.getDocument("doc1", false);
    assertNotNull(d);
    assertTrue(Arrays.equals(SafeEncoder.encode("Hello World!"), (byte[]) d.get("txt1")));
    ops.deleteDocument("doc1");
    dropIndex("testGet");
  }

  @Test
  void testAlias() throws Exception {
    SearchOperations<String> ops = modulesOperations.opsForSearch("testAlias");

    Schema sc = new Schema().addTextField("field1", 1.0);

    IndexDefinition index = new IndexDefinition();
    index.setPrefixes("testAlias:");
    IndexOptions options = Client.IndexOptions.defaultOptions().setDefinition(index);
    assertTrue(ops.createIndex(sc, options));
    Map<String, Object> doc = new HashMap<>();
    doc.put("field1", "value");
    assertTrue(ops.addDocument("testAlias:doc1", doc));

    assertTrue(ops.addAlias("ALIAS1"));
    SearchOperations<String> alias1 = modulesOperations.opsForSearch("ALIAS1");
    SearchResult res1 = alias1.search(new Query("*").returnFields("field1"));
    assertEquals(1, res1.totalResults);
    assertEquals("value", res1.docs.get(0).get("field1"));

    assertTrue(ops.updateAlias("ALIAS2"));
    SearchOperations<String> alias2 = modulesOperations.opsForSearch("ALIAS2");
    SearchResult res2 = alias2.search(new Query("*").returnFields("field1"));
    assertEquals(1, res2.totalResults);
    assertEquals("value", res2.docs.get(0).get("field1"));

    try {
      ops.deleteAlias("ALIAS3");
      fail("Should throw JedisDataException");
    } catch (JedisDataException e) {
      // Alias does not exist
    }
    assertTrue(ops.deleteAlias("ALIAS2"));
    try {
      ops.deleteAlias("ALIAS2");
      fail("Should throw JedisDataException");
    } catch (JedisDataException e) {
      // Alias does not exist
    }
    dropIndex("testAlias");
  }

  @Test
  void testAlterAdd() throws Exception {
    SearchOperations<String> ops = modulesOperations.opsForSearch("testAlterAdd");

    Schema sc = new Schema().addTextField("title", 1.0);

    IndexDefinition index = new IndexDefinition();
    index.setPrefixes("testAlterAdd:");
    IndexOptions options = Client.IndexOptions.defaultOptions().setDefinition(index);
    assertTrue(ops.createIndex(sc, options));

    Map<String, Object> fields = new HashMap<>();
    fields.put("title", "hello world");
    for (int i = 0; i < 100; i++) {
      assertTrue(ops.addDocument(String.format("testAlterAdd:doc%d", i), fields));
    }

    SearchResult res = ops.search(new Query("hello world"));
    assertEquals(100, res.totalResults);

    assertTrue(ops.alterIndex(new TagField("tags", ","), new TextField("name", 0.5)));
    for (int i = 0; i < 100; i++) {
      Map<String, Object> fields2 = new HashMap<>();
      fields2.put("name", "name" + i);
      fields2.put("tags", String.format("tagA,tagB,tag%d", i));
      assertTrue(ops.updateDocument(String.format("testAlterAdd:doc%d", i), 1.0, fields2));
    }
    SearchResult res2 = ops.search(new Query("@tags:{tagA}"));
    assertEquals(100, res2.totalResults);

    Map<String, Object> info = ops.getInfo();
    assertEquals("testAlterAdd", info.get("index_name"));
    assertEquals("identifier", ((List<?>) ((List<?>) info.get("attributes")).get(1)).get(0));
    assertEquals("attribute", ((List<?>) ((List<?>) info.get("attributes")).get(1)).get(2));
  }

  @Test
  void testConfig() throws Exception {
    SearchOperations<String> ops = modulesOperations.opsForSearch("testConfig");

    boolean result = ops.setConfig(ConfigOption.TIMEOUT, "100");
    assertTrue(result);
    Map<String, String> configMap = ops.getAllConfig();
    assertEquals("100", configMap.get(ConfigOption.TIMEOUT.name()));
    assertEquals("100", ops.getConfig(ConfigOption.TIMEOUT));

    ops.setConfig(ConfigOption.ON_TIMEOUT, "fail");
    assertEquals("fail", ops.getConfig(ConfigOption.ON_TIMEOUT));

    try {
      assertFalse(ops.setConfig(ConfigOption.ON_TIMEOUT, "null"));
    } catch (JedisDataException e) {
      // Should throw an exception after RediSearch 2.2
    }
  }

  @Test
  void testSyn() throws Exception {
    SearchOperations<String> ops = modulesOperations.opsForSearch("testSyn");

    Schema sc = new Schema().addTextField("name", 1.0).addTextField("addr", 1.0);
    assertAll( //
        () -> assertTrue(ops.createIndex(sc, Client.IndexOptions.defaultOptions())), //

        () -> assertTrue(ops.updateSynonym("Wisenheimer", "Smarty Pants")), //
        () -> assertTrue(ops.updateSynonym("Knuckle Sandwich", "Punch")), //
        () -> assertTrue(ops.updateSynonym("Ducky Shincracker", "Good Dancer")), //
        () -> assertTrue(ops.updateSynonym("Zozzled", "Drunk", "Inebriated")) //
    );
    Map<String, List<String>> dump = ops.dumpSynonym();

    assertAll( //
        () -> assertThat(dump).contains(entry("drunk", List.of("Zozzled"))), //
        () -> assertThat(dump).contains(entry("smarty pants", List.of("Wisenheimer"))), //
        () -> assertThat(dump).contains(entry("inebriated", List.of("Zozzled"))), //
        () -> assertThat(dump).contains(entry("good dancer", List.of("Ducky Shincracker"))), //
        () -> assertThat(dump).contains(entry("punch", List.of("Knuckle Sandwich"))) //
    );
  }

  @Test
  void testNumericFilter() throws Exception {
    SearchOperations<String> ops = modulesOperations.opsForSearch("testNumericFilter");

    Schema sc = new Schema().addTextField("title", 1.0).addNumericField("price");

    IndexDefinition index = new IndexDefinition();
    index.setPrefixes("testNumericFilter:");
    IndexOptions options = Client.IndexOptions.defaultOptions().setDefinition(index);

    assertTrue(ops.createIndex(sc, options));
    Map<String, Object> fields = new HashMap<>();
    fields.put("title", "hello world");

    for (int i = 0; i < 100; i++) {
      fields.put("price", i);
      assertTrue(ops.addDocument(String.format("testNumericFilter:doc%d", i), fields));
    }

    SearchResult res = ops.search(new Query("hello world").addFilter(new Query.NumericFilter("price", 0, 49)));
    assertEquals(50, res.totalResults);
    assertEquals(10, res.docs.size());
    for (Document d : res.docs) {
      long price = Long.valueOf((String) d.get("price"));
      assertTrue(price >= 0);
      assertTrue(price <= 49);
    }

    res = ops.search(new Query("hello world").addFilter(new Query.NumericFilter("price", 0, true, 49, true)));
    assertEquals(48, res.totalResults);
    assertEquals(10, res.docs.size());
    for (Document d : res.docs) {
      long price = Long.valueOf((String) d.get("price"));
      assertTrue(price > 0);
      assertTrue(price < 49);
    }
    res = ops.search(new Query("hello world").addFilter(new Query.NumericFilter("price", 50, 100)));
    assertEquals(50, res.totalResults);
    assertEquals(10, res.docs.size());
    for (Document d : res.docs) {
      long price = Long.valueOf((String) d.get("price"));
      assertTrue(price >= 50);
      assertTrue(price <= 100);
    }

    res = ops
        .search(new Query("hello world").addFilter(new Query.NumericFilter("price", 20, Double.POSITIVE_INFINITY)));
    assertEquals(80, res.totalResults);
    assertEquals(10, res.docs.size());

    res = ops
        .search(new Query("hello world").addFilter(new Query.NumericFilter("price", Double.NEGATIVE_INFINITY, 10)));
    assertEquals(11, res.totalResults);
    assertEquals(10, res.docs.size());
    dropIndex("testNumericFilter");
  }

  private void dropIndex(String idx) {
    SearchOperations<String> ops = modulesOperations.opsForSearch(idx);
    try {
      ops.dropIndex();
    } catch (JedisDataException jdee) {
      // IGNORE: Unknown Index name
    }
  }

  private static Map<String, String> toMap(String... values) {
    Map<String, String> map = new HashMap<>();
    for (int i = 0; i < values.length; i += 2) {
      map.put(values[i], values[i + 1]);
    }
    return map;
  }
}
