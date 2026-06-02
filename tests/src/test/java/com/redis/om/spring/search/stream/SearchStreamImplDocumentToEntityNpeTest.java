package com.redis.om.spring.search.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
 * Unit test for issue #734:
 * SearchStreamImpl.documentToEntity() previously called rawJson.toString() without a null
 * check. When Redis does not include "$" in the FT.SEARCH response (e.g. the
 * index was created externally and the query specifies explicit RETURN fields
 * that omit the root JSON projection), documentToEntity() used to throw a
 * NullPointerException.
 *
 * <p>This test wires up a minimal SearchStreamImpl backed by mock
 * SearchOperations that return a Document with no "$" field, reproducing the
 * exact scenario described in the issue <em>without</em> requiring a running
 * Redis instance or a specific Redis version.</p>
 *
 * <p>After the fix: documents with a missing "$" field are silently skipped
 * (a warning is logged) and collect() returns an empty list instead of
 * throwing NPE.</p>
 */
class SearchStreamImplDocumentToEntityNpeTest {

  @Test
  @SuppressWarnings("unchecked")
  void collectSkipsDocumentWhenRootJsonFieldAbsent() throws Exception {
    // --- mocks ---
    SearchOperations<String> searchOps = mock(SearchOperations.class);
    JSONOperations<String> jsonOps = mock(JSONOperations.class);
    StringRedisTemplate template = mock(StringRedisTemplate.class);
    RedisModulesOperations<String> modulesOps = mock(RedisModulesOperations.class);
    RediSearchIndexer indexer = mock(RediSearchIndexer.class);

    // Simulate FT.INFO: a JSON-type index
    Map<String, Object> indexInfo = new HashMap<>();
    indexInfo.put("index_definition", Arrays.asList("key_type", "JSON", "prefixes", Collections.emptyList()));
    when(modulesOps.opsForSearch(anyString())).thenReturn(searchOps);
    when(modulesOps.opsForJSON()).thenReturn(jsonOps);
    when(modulesOps.template()).thenReturn(template);
    when(searchOps.getInfo()).thenReturn(indexInfo);

    // A document whose "$" field is absent (simulates Redis returning no root
    // JSON payload — e.g. because of stale / non-JSON keys that share the index
    // prefix on a reused Testcontainers instance, or externally-created indexes
    // that omit the root projection).
    Document docWithNullJson = new Document("bicycle:0", Collections.emptyMap());

    SearchResult searchResult = mock(SearchResult.class);
    when(searchResult.getDocuments()).thenReturn(List.of(docWithNullJson));
    when(searchOps.search(any())).thenReturn(searchResult);

    // Resolve the id field on the entity class
    Field idField = Bicycle.class.getDeclaredField("id");
    idField.setAccessible(true);

    SearchStreamImpl<Bicycle> stream = new SearchStreamImpl<>(Bicycle.class, "idx:bicycle", idField, modulesOps,
        new GsonBuilder(), indexer);

    // After the fix: null rawJson → warning logged → entity skipped → empty result list.
    // No NullPointerException should be thrown.
    List<Bicycle> result = stream.collect(Collectors.toList());
    assertThat(result).isEmpty();
  }

  // Minimal entity — intentionally has no redis-om-spring annotations so the
  // index is treated as "externally created" (detached mode).
  @Data
  static class Bicycle {
    private String id;
    private String brand;
    private String model;
    private BigDecimal price;
  }
}
