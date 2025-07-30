package com.redis.om.spring.annotations;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.LexicographicDoc;
import com.redis.om.spring.fixtures.document.model.LexicographicDoc$;
import com.redis.om.spring.fixtures.document.repository.LexicographicDocRepository;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.search.stream.EntityStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.search.Schema;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for lexicographic indexing feature on @Indexed and @Searchable annotations.
 * This feature enables sorted set backing for efficient string range queries.
 */
class LexicographicIndexTest extends AbstractBaseDocumentTest {
  private static final Logger logger = LoggerFactory.getLogger(LexicographicIndexTest.class);

  @Autowired
  LexicographicDocRepository repository;

  @Autowired
  RediSearchIndexer indexer;

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @Autowired
  RedisTemplate<String, String> redisTemplate;

  @Autowired
  EntityStream entityStream;

  private String entityPrefix;

  @BeforeEach
  void setup() {
    repository.deleteAll();
    
    // Force index recreation to ensure sorted sets are created
    indexer.dropAndRecreateIndexFor(LexicographicDoc.class);
    
    entityPrefix = indexer.getKeyspaceForEntityClass(LexicographicDoc.class);
    assertThat(entityPrefix).isNotNull().isNotEmpty();

    // Create test data with explicit IDs
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
  }

  @Test
  void testIndexedAnnotationHasLexicographicParameter() throws NoSuchFieldException {
    // Test that @Indexed annotation has lexicographic parameter
    Field skuField = LexicographicDoc.class.getDeclaredField("sku");
    Indexed indexed = skuField.getAnnotation(Indexed.class);
    
    assertNotNull(indexed, "@Indexed annotation should be present on sku field");
    assertTrue(indexed.lexicographic(), "lexicographic parameter should be true");
  }

  @Test
  void testSearchableAnnotationHasLexicographicParameter() throws NoSuchFieldException {
    // Test that @Searchable annotation has lexicographic parameter
    Field nameField = LexicographicDoc.class.getDeclaredField("name");
    Searchable searchable = nameField.getAnnotation(Searchable.class);
    
    assertNotNull(searchable, "@Searchable annotation should be present on name field");
    assertTrue(searchable.lexicographic(), "lexicographic parameter should be true");
  }

  @Test
  void testSortedSetCreatedForLexicographicFields() {
    // Verify that sorted sets are created for fields with lexicographic=true
    String skuLexKey = entityPrefix + "sku:lex";
    String nameLexKey = entityPrefix + "name:lex";
    String categoryLexKey = entityPrefix + "category:lex";
    String statusLexKey = entityPrefix + "status:lex"; // Should not exist

    assertTrue(redisTemplate.hasKey(skuLexKey), "Sorted set for sku field should exist");
    assertTrue(redisTemplate.hasKey(nameLexKey), "Sorted set for name field should exist");
    assertTrue(redisTemplate.hasKey(categoryLexKey), "Sorted set for category field should exist");
    assertFalse(redisTemplate.hasKey(statusLexKey), "Sorted set for status field should not exist (lexicographic=false)");
  }

  @Test
  void testSortedSetContainsCorrectEntries() {
    String skuLexKey = entityPrefix + "sku:lex";
    logger.debug("Checking sorted set key: '{}'", skuLexKey);
    
    // Check sorted set members
    Set<String> members = redisTemplate.opsForZSet().range(skuLexKey, 0, -1);
    assertNotNull(members);
    logger.debug("Sorted set members: {}", members);
    
    // After the saveAll in setup, we should have 5 entries
    assertEquals(5, members.size(), "Should have 5 entries in sorted set");
    
    // Verify member format (value#id)
    assertTrue(members.stream().anyMatch(m -> m.startsWith("product001#")), 
      "Should contain entry for product001");
    assertTrue(members.stream().anyMatch(m -> m.startsWith("product002#")), 
      "Should contain entry for product002");
    assertTrue(members.stream().anyMatch(m -> m.startsWith("product003#")), 
      "Should contain entry for product003");
    assertTrue(members.stream().anyMatch(m -> m.startsWith("product004#")), 
      "Should contain entry for product004");
    assertTrue(members.stream().anyMatch(m -> m.startsWith("product005#")), 
      "Should contain entry for product005");
    
    // Verify lexicographic ordering
    // Get entries in sorted set order (should already be lexicographic)
    List<String> entriesInOrder = new ArrayList<>(members);
    List<String> entriesSorted = entriesInOrder.stream().sorted().collect(Collectors.toList());
    assertEquals(entriesSorted, entriesInOrder, 
      "Members should be in lexicographic order");
  }

  @Test
  void testRepositoryMethodFindBySkuGreaterThan() {
    List<LexicographicDoc> results = repository.findBySkuGreaterThan("product002");
    
    assertThat(results).hasSize(3);
    assertThat(results.stream().map(LexicographicDoc::getSku))
      .containsExactlyInAnyOrder("product003", "product004", "product005");
  }

  @Test
  void testRepositoryMethodFindByNameLessThan() {
    List<LexicographicDoc> results = repository.findByNameLessThan("Product Delta");
    
    assertThat(results).hasSize(2);
    assertThat(results.stream().map(LexicographicDoc::getName))
      .containsExactlyInAnyOrder("Product Alpha", "Product Beta");
  }

  @Test
  void testRepositoryMethodFindBySkuBetween() {
    List<LexicographicDoc> results = repository.findBySkuBetween("product002", "product004");
    
    assertThat(results).hasSize(3);
    assertThat(results.stream().map(LexicographicDoc::getSku))
      .containsExactlyInAnyOrder("product002", "product003", "product004");
  }

  @Test
  void testEntityStreamGtMethodOnTextTagField() {
    var stream = entityStream.of(LexicographicDoc.class);
    logger.debug("Initial backing query: '{}'", stream.backingQuery());
    
    logger.debug("SKU field type: {}", LexicographicDoc$.SKU.getClass().getName());
    var predicate = LexicographicDoc$.SKU.gt("product003");
    logger.debug("Predicate class: {}", predicate.getClass().getName());
    logger.debug("Predicate simple name: {}", predicate.getClass().getSimpleName());
    logger.debug("Is LexicographicPredicate? {}", (predicate instanceof com.redis.om.spring.search.stream.predicates.lexicographic.LexicographicPredicate));
    logger.debug("Is LexicographicGreaterThanMarker? {}", (predicate instanceof com.redis.om.spring.search.stream.predicates.lexicographic.LexicographicGreaterThanMarker));
    
    var filteredStream = stream.filter(predicate);
    logger.debug("After filter backing query: '{}'", filteredStream.backingQuery());
    
    List<LexicographicDoc> results = filteredStream.collect(Collectors.toList());
    logger.debug("Results count: {}", results.size());
    results.forEach(r -> logger.debug("Result: {} (id={})", r.getSku(), r.getId()));
    
    assertThat(results).hasSize(2);
    assertThat(results.stream().map(LexicographicDoc::getSku))
      .containsExactlyInAnyOrder("product004", "product005");
  }

  @Test
  void testEntityStreamLtMethodOnTextField() {
    List<LexicographicDoc> results = entityStream
      .of(LexicographicDoc.class)
      .filter(LexicographicDoc$.NAME.lt("Product Gamma"))
      .collect(Collectors.toList());
    
    assertThat(results).hasSize(4);
    assertThat(results.stream().map(LexicographicDoc::getName))
      .containsExactlyInAnyOrder("Product Alpha", "Product Beta", "Product Delta", "Product Epsilon");
  }

  @Test
  void testEntityStreamBetweenMethod() {
    List<LexicographicDoc> results = entityStream
      .of(LexicographicDoc.class)
      .filter(LexicographicDoc$.CATEGORY.between("Books", "Electronics"))
      .collect(Collectors.toList());
    
    assertThat(results).hasSize(4); // Two Electronics entries (id=1 and id=4), plus Books and Clothing
    assertThat(results.stream().map(LexicographicDoc::getCategory))
      .containsExactlyInAnyOrder("Electronics", "Books", "Clothing", "Electronics");
  }

  @Test
  void testUpdateMaintainsSortedSet() {
    // First, check what's in the sorted set before updating
    String skuLexKey = entityPrefix + "sku:lex";
    Set<String> beforeMembers = redisTemplate.opsForZSet().range(skuLexKey, 0, -1);
    logger.debug("Before update: {}", beforeMembers);
    
    // Update a document
    LexicographicDoc doc = repository.findById("1").orElseThrow();
    String oldSku = doc.getSku();
    logger.debug("Found doc with SKU: {}", oldSku);
    doc.setSku("product000"); // Change to come before all others
    repository.save(doc);
    
    // Verify sorted set was updated
    Set<String> afterMembers = redisTemplate.opsForZSet().range(skuLexKey, 0, -1);
    logger.debug("After update: {}", afterMembers);
    
    // Old entry should be removed
    assertFalse(afterMembers.stream().anyMatch(m -> m.startsWith(oldSku + "#")), 
      "Old SKU entry should be removed");
    
    // New entry should exist
    assertTrue(afterMembers.stream().anyMatch(m -> m.startsWith("product000#")), 
      "New SKU entry should exist");
    
    // Should still have 5 entries after update
    assertEquals(5, afterMembers.size(), "Should still have 5 entries after update");
  }

  @Test
  void testDeleteRemovesFromSortedSet() {
    // First, check what's in the sorted set before deleting
    String skuLexKey = entityPrefix + "sku:lex";
    Set<String> beforeMembers = redisTemplate.opsForZSet().range(skuLexKey, 0, -1);
    logger.debug("Before delete: {}", beforeMembers);
    
    // Delete a document
    repository.deleteById("3");
    
    // Verify sorted set was updated
    Set<String> afterMembers = redisTemplate.opsForZSet().range(skuLexKey, 0, -1);
    logger.debug("After delete: {}", afterMembers);
    
    // Should have 4 entries now  
    assertEquals(4, afterMembers.size(), "Should have 4 entries after delete");
    
    // Deleted entry should not exist
    assertFalse(afterMembers.stream().anyMatch(m -> m.contains("#3")), 
      "Deleted document entry should not exist");
  }

  // Tests for @Searchable(lexicographic=true) field
  @Test
  void testSearchableLexicographicGreaterThan() {
    // Test repository method for name field (TEXT field with lexicographic=true)
    List<LexicographicDoc> results = repository.findByNameGreaterThan("Product Beta");
    
    assertThat(results).hasSize(3);
    assertThat(results.stream().map(LexicographicDoc::getName))
      .containsExactlyInAnyOrder("Product Gamma", "Product Delta", "Product Epsilon");
  }

  @Test
  void testSearchableLexicographicLessThan() {
    // Test that findByNameLessThan works correctly
    List<LexicographicDoc> results = repository.findByNameLessThan("Product Delta");
    
    assertThat(results).hasSize(2);
    assertThat(results.stream().map(LexicographicDoc::getName))
      .containsExactlyInAnyOrder("Product Alpha", "Product Beta");
  }

  @Test
  void testSearchableLexicographicBetween() {
    // Test repository method for name field between range
    List<LexicographicDoc> results = repository.findByNameBetween("Product Beta", "Product Delta");
    
    assertThat(results).hasSize(2);
    assertThat(results.stream().map(LexicographicDoc::getName))
      .containsExactlyInAnyOrder("Product Beta", "Product Delta");
  }

  @Test
  void testEntityStreamWithSearchableLexicographic() {
    // Test EntityStream with TextField gt method
    List<LexicographicDoc> results = entityStream
      .of(LexicographicDoc.class)
      .filter(LexicographicDoc$.NAME.gt("Product Beta"))
      .collect(Collectors.toList());
    
    assertThat(results).hasSize(3);
    assertThat(results.stream().map(LexicographicDoc::getName))
      .containsExactlyInAnyOrder("Product Gamma", "Product Delta", "Product Epsilon");
  }

  @Test
  void testEntityStreamSearchableLexicographicBetween() {
    // Test EntityStream with TextField between method
    List<LexicographicDoc> results = entityStream
      .of(LexicographicDoc.class)
      .filter(LexicographicDoc$.NAME.between("Product Alpha", "Product Gamma"))
      .sorted(LexicographicDoc$.NAME)
      .collect(Collectors.toList());
    
    assertThat(results).hasSize(5);
    assertThat(results.stream().map(LexicographicDoc::getName))
      .containsExactly("Product Alpha", "Product Beta", "Product Delta", "Product Epsilon", "Product Gamma");
  }

  @Test
  void testSearchableLexicographicSortedSetCreated() {
    // Verify that a sorted set is created for the name field
    String nameLexKey = entityPrefix + "name:lex";
    
    // Check that the sorted set exists
    Long size = redisTemplate.opsForZSet().size(nameLexKey);
    assertNotNull(size);
    assertEquals(5L, size, "Should have 5 entries in name lexicographic sorted set");
    
    // Verify the entries are in correct order
    Set<String> members = redisTemplate.opsForZSet().range(nameLexKey, 0, -1);
    assertNotNull(members);
    assertEquals(5, members.size());
    
    // Convert to list to check order
    List<String> orderedMembers = new ArrayList<>(members);
    assertTrue(orderedMembers.get(0).startsWith("Product Alpha#"));
    assertTrue(orderedMembers.get(1).startsWith("Product Beta#"));
    assertTrue(orderedMembers.get(2).startsWith("Product Delta#"));
    assertTrue(orderedMembers.get(3).startsWith("Product Epsilon#"));
    assertTrue(orderedMembers.get(4).startsWith("Product Gamma#"));
  }
}