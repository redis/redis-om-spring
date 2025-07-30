package com.redis.om.spring.annotations;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.LexicographicDoc;
import com.redis.om.spring.fixtures.document.repository.LexicographicDocRepository;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.repository.query.RediSearchQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal test for lexicographic queries with full debug logging
 */
class LexicographicMinimalTest extends AbstractBaseDocumentTest {

  @Autowired
  LexicographicDocRepository repository;

  @Autowired
  RediSearchIndexer indexer;

  @BeforeEach
  void setup() {
    repository.deleteAll();
    
    // Enable debug logging for RediSearchQuery and LexicographicQueryExecutor
    ch.qos.logback.classic.Logger queryLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(RediSearchQuery.class);
    queryLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
    
    ch.qos.logback.classic.Logger lexLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("com.redis.om.spring.repository.query.lexicographic");
    lexLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
    
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
  }

  @Test
  void testFindBySkuGreaterThan() {
    System.out.println("\n=== TEST FIND BY SKU GREATER THAN ===");
    
    List<LexicographicDoc> results = repository.findBySkuGreaterThan("product001");
    
    System.out.println("Results size: " + results.size());
    for (LexicographicDoc doc : results) {
      System.out.println("  Result: " + doc.getSku() + " - " + doc.getName());
    }
    
    assertThat(results).hasSize(2);
    assertThat(results.stream().map(LexicographicDoc::getSku))
      .containsExactlyInAnyOrder("product002", "product003");
  }
}