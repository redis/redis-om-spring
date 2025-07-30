package com.redis.om.spring.annotations;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.LexicographicDoc;
import com.redis.om.spring.fixtures.document.repository.LexicographicDocRepository;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Debug test for lexicographic feature
 */
class LexicographicDebugTest extends AbstractBaseDocumentTest {

  @Autowired
  LexicographicDocRepository repository;

  @Autowired
  RediSearchIndexer indexer;

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @Autowired
  RedisTemplate<String, String> redisTemplate;

  @BeforeEach
  void setup() {
    repository.deleteAll();
    
    // Force index recreation
    indexer.dropAndRecreateIndexFor(LexicographicDoc.class);
    
    // Create test data
    LexicographicDoc doc1 = LexicographicDoc.of("product001", "Product Alpha", "Electronics", "Active");
    doc1.setId("1");
    LexicographicDoc doc2 = LexicographicDoc.of("product002", "Product Beta", "Books", "Active");
    doc2.setId("2");
    LexicographicDoc doc3 = LexicographicDoc.of("product003", "Product Gamma", "Clothing", "Inactive");
    doc3.setId("3");
    
    repository.saveAll(Arrays.asList(doc1, doc2, doc3));
    
    // Debug: Check sorted set
    String entityPrefix = indexer.getKeyspaceForEntityClass(LexicographicDoc.class);
    String skuLexKey = entityPrefix + "sku:lex";
    Set<String> members = redisTemplate.opsForZSet().range(skuLexKey, 0, -1);
    System.out.println("Sorted set members: " + members);
  }

  @Test
  void debugRepositoryQuery() {
    System.out.println("\n=== DEBUG REPOSITORY QUERY ===");
    
    // Enable debug logging
    ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
    
    ch.qos.logback.classic.Logger queryLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("com.redis.om.spring.repository.query");
    queryLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
    
    // Try the query
    List<LexicographicDoc> results = repository.findBySkuGreaterThan("product001");
    
    System.out.println("Query results size: " + results.size());
    for (LexicographicDoc doc : results) {
      System.out.println("  Result: " + doc.getSku());
    }
    
    assertThat(results).hasSize(2);
  }
}