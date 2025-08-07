package com.redis.om.spring.annotations;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.LexicographicDoc;
import com.redis.om.spring.fixtures.document.repository.LexicographicDocRepository;
import com.redis.om.spring.indexing.RediSearchIndexer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Debug test for lexicographic LessThan queries
 */
class LexicographicLessThanDebugTest extends AbstractBaseDocumentTest {

  @Autowired
  LexicographicDocRepository repository;

  @Autowired
  RediSearchIndexer indexer;

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
    LexicographicDoc doc4 = LexicographicDoc.of("product004", "Product Delta", "Electronics", "Active");
    doc4.setId("4");
    LexicographicDoc doc5 = LexicographicDoc.of("product005", "Product Epsilon", "Food", "Pending");
    doc5.setId("5");
    
    repository.saveAll(Arrays.asList(doc1, doc2, doc3, doc4, doc5));
    
    // Debug: Check sorted set for name field
    String entityPrefix = indexer.getKeyspaceForEntityClass(LexicographicDoc.class);
    String nameLexKey = entityPrefix + "name:lex";
    Set<String> members = redisTemplate.opsForZSet().range(nameLexKey, 0, -1);
    System.out.println("Name sorted set members: " + members);
    
    // Test direct ZRANGEBYLEX
    Set<String> directQuery = redisTemplate.opsForZSet().rangeByLex(nameLexKey,
        Range.leftUnbounded(Range.Bound.exclusive("Product Delta#")));
    System.out.println("Direct ZRANGEBYLEX LT 'Product Delta#': " + directQuery);
  }

  @Test
  void testLessThanQuery() {
    System.out.println("\n=== TEST LESS THAN QUERY ===");
    
    List<LexicographicDoc> results = repository.findByNameLessThan("Product Delta");
    
    System.out.println("Results size: " + results.size());
    for (LexicographicDoc doc : results) {
      System.out.println("  Result: " + doc.getName() + " (id=" + doc.getId() + ")");
    }
    
    // Check lexicographic ordering manually
    String[] names = {"Product Alpha", "Product Beta", "Product Gamma", "Product Delta", "Product Epsilon"};
    System.out.println("\nLexicographic comparison with 'Product Delta':");
    for (String name : names) {
      int comparison = name.compareTo("Product Delta");
      System.out.println("  '" + name + "'.compareTo('Product Delta') = " + comparison + " (" + (comparison < 0 ? "LESS" : comparison > 0 ? "GREATER" : "EQUAL") + ")");
    }
  }
}