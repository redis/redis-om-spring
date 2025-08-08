package com.redis.om.spring.issues;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.google.gson.JsonObject;
import com.redis.om.spring.AbstractBaseOMTest;
import com.redis.om.spring.TestConfig;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.fixtures.document.model.MyDoc;
import com.redis.om.spring.fixtures.document.repository.MyDocRepository;
import com.redis.om.spring.mapping.RedisEnhancedMappingContext;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.search.SearchOperations;

/**
 * Test for issue #637: "Not able to override keyValueMappingContext"
 * 
 * This test verifies that users can now provide their own RedisEnhancedMappingContext
 * bean with a custom keyspace resolver, without needing to set
 * spring.main.allow-bean-definition-overriding=true
 * 
 * The fix removes @Primary and adds @ConditionalOnMissingBean to allow user overrides.
 */
@Testcontainers
@DirtiesContext
@SpringBootTest(classes = Issue637CustomMappingContextTest.Config.class)
class Issue637CustomMappingContextTest extends AbstractBaseOMTest {

  private static final String TENANT_PREFIX = "tenant_prod";

  @Autowired
  MyDocRepository myDocRepository;

  @Autowired
  RedisTemplate<String, String> template;

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @Autowired
  RedisMappingContext mappingContext;
  
  String myDocId;

  @BeforeEach
  void loadTestData() {
    Point point = new Point(-122.124500, 47.640160);
    MyDoc myDoc = MyDoc.of("issue 637 test", point, point, 1);
    myDoc = myDocRepository.save(myDoc);
    myDocId = myDoc.getId();
  }

  @AfterEach
  void cleanUp() {
    myDocRepository.deleteAll();
  }

  @Test
  void testIssue637_CustomMappingContextIsUsed() {
    // Verify our custom mapping context is being used
    assertThat(mappingContext).isInstanceOf(RedisEnhancedMappingContext.class);
    
    // Verify the custom keyspace resolver is applied
    String keyspace = mappingContext.getKeySpaceResolver().resolveKeySpace(MyDoc.class);
    assertThat(keyspace).isEqualTo(TENANT_PREFIX + ":MyDoc");
  }

  @Test
  void testIssue637_DocumentsUseCustomKeyspace() {
    // Verify the document was saved with custom keyspace
    JSONOperations<String> ops = modulesOperations.opsForJSON();
    
    // The key should use our custom prefix
    String expectedKey = TENANT_PREFIX + ":MyDoc:" + myDocId;
    JsonObject rawJSON = ops.get(expectedKey, JsonObject.class);
    
    assertNotNull(rawJSON, "Document should exist at custom keyspace");
    assertEquals(myDocId, rawJSON.get("id").getAsString());
  }

  @Test
  void testIssue637_SearchIndexUsesCustomKeyspace() {
    SearchOperations<String> searchOps = modulesOperations.opsForSearch(MyDoc.class.getName() + "Idx");
    var info = searchOps.getInfo();
    
    @SuppressWarnings("unchecked")
    var definition = (List<Object>) info.get("index_definition");
    assertNotNull(definition);
    
    int prefixesIndex = definition.indexOf("prefixes");
    assertTrue(prefixesIndex >= 0, "Index definition should contain prefixes");
    
    @SuppressWarnings("unchecked")
    var prefixes = (List<String>) definition.get(prefixesIndex + 1);
    assertNotNull(prefixes);
    assertEquals(1, prefixes.size());
    assertEquals(TENANT_PREFIX + ":MyDoc:", prefixes.get(0), 
        "Index should use custom keyspace prefix");
  }

  @Test
  void testIssue637_RepositoryOperationsWork() {
    // Test find by ID
    Optional<MyDoc> maybeDoc = myDocRepository.findById(myDocId);
    assertTrue(maybeDoc.isPresent());
    assertEquals("issue 637 test", maybeDoc.get().getTitle());
    
    // Test update
    MyDoc doc = maybeDoc.get();
    doc.setTitle("updated title");
    myDocRepository.save(doc);
    
    maybeDoc = myDocRepository.findById(myDocId);
    assertTrue(maybeDoc.isPresent());
    assertEquals("updated title", maybeDoc.get().getTitle());
    
    // Test delete
    myDocRepository.deleteById(myDocId);
    maybeDoc = myDocRepository.findById(myDocId);
    assertFalse(maybeDoc.isPresent());
  }

  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(
      basePackages = { 
          "com.redis.om.spring.fixtures.document.model",
          "com.redis.om.spring.fixtures.document.repository" 
      }
  )
  static class Config extends TestConfig {
    
    /**
     * This demonstrates the fix for issue #637.
     * Users can now provide their own RedisEnhancedMappingContext bean
     * with a custom keyspace resolver.
     * 
     * The @ConditionalOnMissingBean annotation on the default bean
     * ensures this user-provided bean takes precedence without
     * requiring spring.main.allow-bean-definition-overriding=true
     */
    @Bean(name = "redisEnhancedMappingContext") 
    @Primary
    public RedisEnhancedMappingContext customMappingContext() {
      RedisEnhancedMappingContext mappingContext = new RedisEnhancedMappingContext();
      
      // Custom keyspace resolver for multi-tenant support
      mappingContext.setKeySpaceResolver(type -> 
          TENANT_PREFIX + ":" + type.getSimpleName());
      
      return mappingContext;
    }
  }
}