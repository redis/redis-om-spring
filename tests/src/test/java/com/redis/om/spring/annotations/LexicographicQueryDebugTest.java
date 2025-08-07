package com.redis.om.spring.annotations;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.LexicographicDoc;
import com.redis.om.spring.fixtures.document.repository.LexicographicDocRepository;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.repository.query.RediSearchQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Debug test for lexicographic query processing
 */
class LexicographicQueryDebugTest extends AbstractBaseDocumentTest {

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
    
    // Enable debug logging
    ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
    
    ch.qos.logback.classic.Logger queryLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(RediSearchQuery.class);
    queryLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
    
    ch.qos.logback.classic.Logger indexerLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(RediSearchIndexer.class);
    indexerLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
    
    // Force index recreation
    indexer.dropAndRecreateIndexFor(LexicographicDoc.class);
    
    // Create test data
    LexicographicDoc doc1 = LexicographicDoc.of("product001", "Product Alpha", "Electronics", "Active");
    doc1.setId("1");
    LexicographicDoc doc2 = LexicographicDoc.of("product002", "Product Beta", "Books", "Active");
    doc2.setId("2");
    LexicographicDoc doc3 = LexicographicDoc.of("product003", "Product Gamma", "Clothing", "Inactive");
    doc3.setId("3");
    LexicographicDoc doc4 = LexicographicDoc.of("product004", "Product Delta", "Electronics", "Active");
    doc4.setId("4");
    LexicographicDoc doc5 = LexicographicDoc.of("product005", "Product Epsilon", "Books", "Inactive");
    doc5.setId("5");
    
    repository.saveAll(Arrays.asList(doc1, doc2, doc3, doc4, doc5));
    
    // Debug: Check sorted set
    String entityPrefix = indexer.getKeyspaceForEntityClass(LexicographicDoc.class);
    String skuLexKey = entityPrefix + "sku:lex";
    Set<String> members = redisTemplate.opsForZSet().range(skuLexKey, 0, -1);
    System.out.println("Sorted set key: " + skuLexKey);
    System.out.println("Sorted set members: " + members);
    
    // Check if documents exist
    long count = repository.count();
    System.out.println("Total documents: " + count);
    
    // Test direct sorted set query
    Set<String> directQuery = redisTemplate.opsForZSet().rangeByLex(skuLexKey,
        Range.rightUnbounded(Range.Bound.exclusive("product002")));
    System.out.println("Direct ZRANGEBYLEX result: " + directQuery);
  }

  @Test
  void debugRepositoryQuery() {
    System.out.println("\n=== DEBUG REPOSITORY QUERY ===");
    
    // Try the query
    List<LexicographicDoc> results = repository.findBySkuGreaterThan("product002");
    
    System.out.println("Query results size: " + results.size());
    for (LexicographicDoc doc : results) {
      System.out.println("  Result: " + doc.getSku() + " - " + doc.getName());
    }
    
    assertThat(results).hasSize(3);
    assertThat(results.stream().map(LexicographicDoc::getSku))
      .containsExactlyInAnyOrder("product003", "product004", "product005");
  }
  
  @Test
  void debugFindAll() {
    System.out.println("\n=== DEBUG FIND ALL ===");
    
    // Try simple find all
    Iterable<LexicographicDoc> results = repository.findAll();
    
    int count = 0;
    for (LexicographicDoc doc : results) {
      System.out.println("  Result: " + doc.getSku() + " - " + doc.getName());
      count++;
    }
    
    System.out.println("Find all results size: " + count);
    assertThat(count).isEqualTo(5);
  }
}