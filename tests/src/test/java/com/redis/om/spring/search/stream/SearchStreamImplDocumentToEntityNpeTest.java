package com.redis.om.spring.search.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.search.SearchOperations;

import lombok.Data;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.SearchResult;

/**
 * Unit tests for the null-rawJson handling in SearchStreamImpl.documentToEntity().
 *
 * <p>When Redis does not include "$" in the FT.SEARCH response (e.g. the index
 * was created externally, or some Redis versions omit the root JSON projection
 * for certain query shapes), documentToEntity() previously threw NPE at
 * rawJson.toString().</p>
 *
 * <p>After the fix:</p>
 * <ul>
 *   <li>A missing "$" triggers a fallback JSON.GET for the document key.</li>
 *   <li>If JSON.GET succeeds the entity is returned normally.</li>
 *   <li>If JSON.GET also returns null (key deleted / not a JSON document) the
 *       document is silently skipped and a warning is logged.</li>
 * </ul>
 */
class SearchStreamImplDocumentToEntityNpeTest {

  // -------------------------------------------------------------------------
  // Shared mock wiring helpers
  // -------------------------------------------------------------------------

  @SuppressWarnings("unchecked")
  private SearchStreamImpl<Bicycle> buildStream(SearchOperations<String> searchOps,
      JSONOperations<String> jsonOps) throws Exception {
    StringRedisTemplate template = mock(StringRedisTemplate.class);
    RedisModulesOperations<String> modulesOps = mock(RedisModulesOperations.class);
    RediSearchIndexer indexer = mock(RediSearchIndexer.class);

    Map<String, Object> indexInfo = new HashMap<>();
    indexInfo.put("index_definition", Arrays.asList("key_type", "JSON", "prefixes", Collections.emptyList()));
    when(modulesOps.opsForSearch(anyString())).thenReturn(searchOps);
    when(modulesOps.opsForJSON()).thenReturn(jsonOps);
    when(modulesOps.template()).thenReturn(template);
    when(searchOps.getInfo()).thenReturn(indexInfo);

    Field idField = Bicycle.class.getDeclaredField("id");
    idField.setAccessible(true);

    return new SearchStreamImpl<>(Bicycle.class, "idx:bicycle", idField, modulesOps, new GsonBuilder(), indexer);
  }

  // -------------------------------------------------------------------------
  // Test: fallback to JSON.GET when "$" is absent, and JSON.GET succeeds
  // -------------------------------------------------------------------------

  @Test
  @SuppressWarnings("unchecked")
  void collectFallsBackToJsonGetWhenRootJsonFieldAbsent() throws Exception {
    SearchOperations<String> searchOps = mock(SearchOperations.class);
    JSONOperations<String> jsonOps = mock(JSONOperations.class);

    // FT.SEARCH returns a document with no "$" field
    Document docWithNullJson = new Document("bicycle:0", Collections.emptyMap());
    SearchResult searchResult = mock(SearchResult.class);
    when(searchResult.getDocuments()).thenReturn(List.of(docWithNullJson));
    when(searchOps.search(any())).thenReturn(searchResult);

    // JSON.GET fallback returns the actual entity
    Bicycle expected = new Bicycle();
    expected.setId("bicycle:0");
    expected.setBrand("Velorim");
    expected.setModel("Jigger");
    expected.setPrice(new BigDecimal("270"));
    when(jsonOps.get(eq("bicycle:0"), eq(Bicycle.class))).thenReturn(expected);

    SearchStreamImpl<Bicycle> stream = buildStream(searchOps, jsonOps);
    List<Bicycle> result = stream.collect(Collectors.toList());

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getModel()).isEqualTo("Jigger");
    assertThat(result.get(0).getBrand()).isEqualTo("Velorim");
  }

  // -------------------------------------------------------------------------
  // Test: document silently skipped when both "$" and JSON.GET return null
  // -------------------------------------------------------------------------

  @Test
  @SuppressWarnings("unchecked")
  void collectSkipsDocumentWhenRootJsonFieldAbsentAndJsonGetReturnsNull() throws Exception {
    SearchOperations<String> searchOps = mock(SearchOperations.class);
    JSONOperations<String> jsonOps = mock(JSONOperations.class);

    // FT.SEARCH returns a document with no "$" field
    Document docWithNullJson = new Document("bicycle:0", Collections.emptyMap());
    SearchResult searchResult = mock(SearchResult.class);
    when(searchResult.getDocuments()).thenReturn(List.of(docWithNullJson));
    when(searchOps.search(any())).thenReturn(searchResult);

    // JSON.GET also returns null — key deleted or not a JSON document
    when(jsonOps.get(eq("bicycle:0"), eq(Bicycle.class))).thenReturn(null);

    SearchStreamImpl<Bicycle> stream = buildStream(searchOps, jsonOps);
    List<Bicycle> result = stream.collect(Collectors.toList());

    // No NPE; document is skipped gracefully
    assertThat(result).isEmpty();
  }

  // -------------------------------------------------------------------------
  // Minimal entity (no redis-om-spring annotations → detached / external index)
  // -------------------------------------------------------------------------

  @Data
  static class Bicycle {
    private String id;
    private String brand;
    private String model;
    private BigDecimal price;
  }
}
