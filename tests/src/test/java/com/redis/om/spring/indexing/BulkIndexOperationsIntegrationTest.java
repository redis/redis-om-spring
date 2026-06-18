package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.test.annotation.DirtiesContext;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

/**
 * Integration tests for bulk index operations in RediSearchIndexer.
 * Tests createIndexes(), dropIndexes(), and listIndexes() methods.
 */
@DirtiesContext
public class BulkIndexOperationsIntegrationTest extends AbstractBaseDocumentTest {

  @Autowired
  private RediSearchIndexer indexer;

  @Test
  void testListIndexes_returnsAllManagedIndexNames() {
    // Given: Several entity classes have been registered with the indexer
    // (The AbstractBaseDocumentTest scans fixtures and creates indexes at startup)

    // When: Listing all managed indexes
    Set<String> managedIndexes = indexer.listIndexes();

    // Then: Should return a non-empty set of index names
    assertThat(managedIndexes)
        .as("listIndexes should return managed index names")
        .isNotNull()
        .isNotEmpty();
  }

  @Test
  void testListIndexes_returnsDefensiveCopy() {
    // When: Getting the list of indexes
    Set<String> managedIndexes = indexer.listIndexes();

    // Then: Should return a HashSet (defensive copy), not internal collection
    assertThat(managedIndexes.getClass().getSimpleName())
        .as("Should return a HashSet copy")
        .isEqualTo("HashSet");
  }

  @Test
  void testCreateIndexes_doesNotThrowException() {
    // Given: Indexes already exist from startup

    // When: Calling createIndexes (should be idempotent)
    // Then: Should not throw exception
    indexer.createIndexes();

    // Verify indexes still exist
    Set<String> indexes = indexer.listIndexes();
    assertThat(indexes).isNotEmpty();
  }

  @Test
  void testCreateIndexes_createsNewlyRegisteredIndex() {
    // Given: A new entity class is registered
    String indexName = indexer.getIndexName(BulkTestEntity1.class);

    // First, ensure it doesn't exist
    if (indexer.indexExistsFor(BulkTestEntity1.class)) {
      indexer.dropIndexFor(BulkTestEntity1.class);
    }

    // Manually add to keyspace mapping to register it
    indexer.addKeySpaceMapping("bulktestentity1", BulkTestEntity1.class);

    // When: Creating all indexes
    indexer.createIndexes();

    // Then: The new index should be created
    assertThat(indexer.indexExistsFor(BulkTestEntity1.class))
        .as("BulkTestEntity1 index should exist after createIndexes()")
        .isTrue();

    // Cleanup
    indexer.dropIndexFor(BulkTestEntity1.class);
  }

  @Test
  void testDropIndexes_removesIndexesFromTracking() {
    // Given: We have some indexes - create a test index first
    indexer.createIndexFor(BulkTestEntity1.class);
    Set<String> indexesBefore = indexer.listIndexes();
    int countBefore = indexesBefore.size();
    assertThat(countBefore).isGreaterThan(0);

    // Verify our test index is in the list
    String testIndexName = BulkTestEntity1.class.getName() + "Idx";
    assertThat(indexesBefore).contains(testIndexName);

    // When: Dropping all indexes
    indexer.dropIndexes();

    // Then: The tracking should be cleared or reduced
    Set<String> indexesAfter = indexer.listIndexes();

    // The test index should no longer be tracked
    assertThat(indexesAfter)
        .as("Test index should be removed from tracking after dropIndexes")
        .doesNotContain(testIndexName);
  }

  @Test
  void testDropIndexes_doesNotThrowWhenCalledMultipleTimes() {
    // Given: Indexes may or may not exist

    // When: Calling dropIndexes multiple times
    // Then: Should not throw exception
    indexer.dropIndexes();
    indexer.dropIndexes();

    // No exception means success
  }

  @Test
  void testCreateIndexForSpecificEntity() {
    // Given: A specific entity class
    String indexName = BulkTestEntity2.class.getName() + "Idx";

    // Ensure clean state
    if (indexer.indexExistsFor(BulkTestEntity2.class)) {
      indexer.dropIndexFor(BulkTestEntity2.class);
    }

    // When: Creating index for specific entity
    indexer.createIndexFor(BulkTestEntity2.class);

    // Then: Index should exist
    assertThat(indexer.indexExistsFor(BulkTestEntity2.class))
        .as("Index should exist after createIndexFor")
        .isTrue();

    assertThat(indexer.listIndexes())
        .as("listIndexes should include the newly created index")
        .contains(indexName);

    // Cleanup
    indexer.dropIndexFor(BulkTestEntity2.class);
  }

  // Test entities for bulk operations
  @Document
  static class BulkTestEntity1 {
    @Id
    private String id;

    @Indexed
    private String name;

    @Indexed
    private String category;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getCategory() {
      return category;
    }

    public void setCategory(String category) {
      this.category = category;
    }
  }

  @Document
  static class BulkTestEntity2 {
    @Id
    private String id;

    @Indexed
    private String title;

    @Indexed
    private Integer count;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public Integer getCount() {
      return count;
    }

    public void setCount(Integer count) {
      this.count = count;
    }
  }
}
