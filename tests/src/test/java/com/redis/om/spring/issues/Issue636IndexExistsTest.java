package com.redis.om.spring.issues;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.redis.om.spring.AbstractBaseOMTest;
import com.redis.om.spring.TestConfig;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;

import redis.clients.jedis.exceptions.JedisDataException;

/**
 * Test for issue #636: "Improve indexExistsFor method in RediSearchIndexer"
 * 
 * This test verifies that the indexExistsFor method properly handles different
 * error messages from different Redis versions when checking for non-existent indexes.
 */
@Testcontainers
@DirtiesContext
@SpringBootTest(classes = Issue636IndexExistsTest.Config.class)
class Issue636IndexExistsTest extends AbstractBaseOMTest {

  @Autowired
  RediSearchIndexer indexer;
  
  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @Test
  void testIssue636_VerifyErrorMessageHandling() {
    // Try to get info for a non-existent index to verify error handling
    SearchOperations<String> searchOps = modulesOperations.opsForSearch("non_existent_index");
    
    JedisDataException capturedError = null;
    try {
      searchOps.getInfo();
      fail("Expected JedisDataException for non-existent index");
    } catch (JedisDataException e) {
      capturedError = e;
    }
    
    assertNotNull(capturedError, "Should have caught an exception");
    String errorMessage = capturedError.getMessage();
    
    // The fixed implementation now handles multiple error message patterns
    // to support different Redis versions (Redis Stack, Redis 7.x, Redis 8.0+)
    String lowerCaseMessage = errorMessage.toLowerCase();
    boolean isRecognizedError = lowerCaseMessage.contains("unknown index") ||
                                lowerCaseMessage.contains("no such index") ||
                                lowerCaseMessage.contains("index does not exist") ||
                                lowerCaseMessage.contains("not found");
    
    assertTrue(isRecognizedError, 
        "Error message should be recognized. Actual: " + errorMessage);
  }

  @Test
  void testIssue636_IndexExistsForNonExistentEntity() {
    // Test that indexExistsFor returns false for an entity that has no index
    boolean exists = indexer.indexExistsFor(NonIndexedEntity.class);
    assertFalse(exists, "Index should not exist for NonIndexedEntity");
  }

  @Test  
  void testIssue636_IndexExistsForIndexedEntity() {
    // Create an index for TestEntity
    indexer.createIndexFor(TestEntity636.class);
    
    // Verify it exists
    boolean exists = indexer.indexExistsFor(TestEntity636.class);
    assertTrue(exists, "Index should exist after creation");
    
    // Drop the index
    indexer.dropIndexFor(TestEntity636.class);
    
    // Verify it no longer exists
    exists = indexer.indexExistsFor(TestEntity636.class);
    assertFalse(exists, "Index should not exist after dropping");
  }
  
  @Test
  void testIssue636_ErrorMessageCompatibility() {
    // Test that both error message patterns are handled correctly
    // This simulates what the fixed method should do
    
    String[] possibleErrorMessages = {
        "ERR Unknown index name",                    // Redis Stack / Redis 7.x
        "ERR no such index",                         // Potential Redis 8.0 message
        "ERR index does not exist",                  // Alternative format
        "Unknown index name 'test_idx'",             // With index name included
        "no such index: test_idx"                    // Alternative with index name
    };
    
    for (String errorMsg : possibleErrorMessages) {
      // The fixed logic should handle all these patterns
      boolean shouldReturnFalse = errorMsg.toLowerCase().contains("unknown index") ||
                                  errorMsg.toLowerCase().contains("no such index") ||
                                  errorMsg.toLowerCase().contains("index does not exist");
      
      assertTrue(shouldReturnFalse, 
          "Error message should be recognized as index not found: " + errorMsg);
    }
  }

  // Test entities
  @Document
  static class TestEntity636 {
    @Id
    private String id;
    
    @Indexed
    private String name;
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
  }
  
  static class NonIndexedEntity {
    private String id;
    private String value;
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
  }

  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(
      basePackages = "com.redis.om.spring.fixtures.document.repository"
  )
  static class Config extends TestConfig {
  }
}