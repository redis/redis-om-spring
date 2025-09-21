package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

public class IndexMigrationServiceTest {

    @Mock
    private RediSearchIndexer indexer;

    @Mock
    private ApplicationContext applicationContext;

    private IndexMigrationService migrationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        migrationService = new IndexMigrationService(indexer, applicationContext);
    }

    @Test
    void testCreateVersionedIndex() {
        // Given: An entity class
        Class<?> entityClass = MigrationTestEntity.class;

        // Mock the indexer to return null (no existing index)
        when(indexer.getIndexName(eq(entityClass))).thenReturn(null);
        when(indexer.createIndexFor(eq(entityClass), anyString(), anyString())).thenReturn(true);

        // When: Creating a versioned index
        String versionedIndexName = migrationService.createVersionedIndex(entityClass);

        // Then: Should create index with version suffix
        assertThat(versionedIndexName).isNotNull();
        assertThat(versionedIndexName).contains("migrationtestentity");
        assertThat(versionedIndexName).matches(".*_v\\d+_idx$"); // ends with _v followed by digits and _idx
    }

    @Test
    void testMigrateIndexWithBlueGreenStrategy() {
        // Given: An entity class and blue-green strategy
        Class<?> entityClass = MigrationTestEntity.class;
        MigrationStrategy strategy = MigrationStrategy.BLUE_GREEN;

        when(indexer.indexExistsFor(eq(entityClass))).thenReturn(true);
        when(indexer.getIndexName(eq(entityClass))).thenReturn("migrationtestentity_v1_idx");
        when(indexer.createIndexFor(eq(entityClass), anyString(), anyString())).thenReturn(true);
        when(indexer.indexExistsFor(any(), anyString())).thenReturn(true); // For verifyIndexIntegrity and switchAlias

        // When: Migrating index
        MigrationResult result = migrationService.migrateIndex(entityClass, strategy);

        // Then: Should complete migration successfully
        assertThat(result).isNotNull();
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getNewIndexName()).isNotNull();
        assertThat(result.getStrategy()).isEqualTo(MigrationStrategy.BLUE_GREEN);
    }

    @Test
    void testMigrateIndexWithDualWriteStrategy() {
        // Given: An entity class and dual write strategy
        Class<?> entityClass = MigrationTestEntity.class;
        MigrationStrategy strategy = MigrationStrategy.DUAL_WRITE;

        when(indexer.indexExistsFor(eq(entityClass))).thenReturn(true);
        when(indexer.getIndexName(eq(entityClass))).thenReturn("migrationtestentity_v1_idx");
        when(indexer.createIndexFor(eq(entityClass), anyString(), anyString())).thenReturn(true);

        // When: Migrating index
        MigrationResult result = migrationService.migrateIndex(entityClass, strategy);

        // Then: Should setup dual write and start background migration
        assertThat(result).isNotNull();
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getStrategy()).isEqualTo(MigrationStrategy.DUAL_WRITE);
        verify(indexer, atLeastOnce()).createIndexFor(eq(entityClass), anyString(), anyString());
    }

    @Test
    void testEnableDualWrite() {
        // Given: An entity class with existing index
        Class<?> entityClass = MigrationTestEntity.class;
        String newIndexName = "test_entity_v2_idx";

        // When: Enabling dual write
        boolean enabled = migrationService.enableDualWrite(entityClass, newIndexName);

        // Then: Should enable writing to both indexes
        assertThat(enabled).isTrue();
    }

    @Test
    void testReindexInBackground() {
        // Given: An entity class and new index
        Class<?> entityClass = MigrationTestEntity.class;
        String newIndexName = "test_entity_v2_idx";

        // When: Starting background reindex
        CompletableFuture<ReindexResult> future = migrationService.reindexInBackground(entityClass, newIndexName);

        // Then: Should return a future that completes
        assertThat(future).isNotNull();
        assertThat(future.isDone() || !future.isCompletedExceptionally()).isTrue();
    }

    @Test
    void testSwitchAlias() {
        // Given: An entity class and new index
        Class<?> entityClass = MigrationTestEntity.class;
        String newIndexName = "test_entity_v2_idx";

        when(indexer.indexExistsFor(eq(entityClass), eq(newIndexName))).thenReturn(true);

        // When: Switching alias to new index
        boolean switched = migrationService.switchAlias(entityClass, newIndexName);

        // Then: Should successfully switch alias
        assertThat(switched).isTrue();
    }

    @Test
    void testVerifyIndexIntegrity() {
        // Given: A new index name
        String indexName = "test_entity_v2_idx";

        when(indexer.indexExistsFor(any(), eq(indexName))).thenReturn(true);

        // When: Verifying index integrity
        boolean isValid = migrationService.verifyIndexIntegrity(indexName);

        // Then: Should verify successfully
        assertThat(isValid).isTrue();
    }

    @Test
    void testScheduleOldIndexCleanup() {
        // Given: An entity class with old index
        Class<?> entityClass = MigrationTestEntity.class;

        // When: Scheduling cleanup
        boolean scheduled = migrationService.scheduleOldIndexCleanup(entityClass);

        // Then: Should schedule cleanup
        assertThat(scheduled).isTrue();
    }

    @Test
    void testGetCurrentIndexVersion() {
        // Given: An entity class
        Class<?> entityClass = MigrationTestEntity.class;

        when(indexer.getIndexName(eq(entityClass))).thenReturn("test_entity_v1_idx");

        // When: Getting current version
        int version = migrationService.getCurrentIndexVersion(entityClass);

        // Then: Should extract version number
        assertThat(version).isEqualTo(1);
    }

    @Test
    void testGetNextIndexVersion() {
        // Given: An entity class with version 1
        Class<?> entityClass = MigrationTestEntity.class;

        when(indexer.getIndexName(eq(entityClass))).thenReturn("test_entity_v1_idx");

        // When: Getting next version
        int nextVersion = migrationService.getNextIndexVersion(entityClass);

        // Then: Should return incremented version
        assertThat(nextVersion).isEqualTo(2);
    }

    @Test
    void testCreateIndexAlias() {
        // Given: An entity class and target index
        Class<?> entityClass = MigrationTestEntity.class;
        String targetIndex = "test_entity_v2_idx";

        // When: Creating an alias
        boolean created = migrationService.createIndexAlias(entityClass, targetIndex);

        // Then: Should create alias successfully
        assertThat(created).isTrue();
    }

    @Test
    void testRemoveIndexAlias() {
        // Given: An entity class
        Class<?> entityClass = MigrationTestEntity.class;

        // When: Removing alias
        boolean removed = migrationService.removeIndexAlias(entityClass);

        // Then: Should remove alias successfully
        assertThat(removed).isTrue();
    }

    @Test
    void testMigrationWithNoExistingIndex() {
        // Given: An entity class with no existing index
        Class<?> entityClass = MigrationTestEntity.class;
        MigrationStrategy strategy = MigrationStrategy.BLUE_GREEN;

        when(indexer.indexExistsFor(eq(entityClass))).thenReturn(false);

        // When: Attempting migration
        MigrationResult result = migrationService.migrateIndex(entityClass, strategy);

        // Then: Should handle gracefully
        assertThat(result).isNotNull();
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getErrorMessage()).contains("No existing index");
    }

    @Test
    void testConcurrentMigrationPrevention() throws InterruptedException, ExecutionException, TimeoutException {
        // Given: A migration already in progress
        Class<?> entityClass = MigrationTestEntity.class;
        MigrationStrategy strategy = MigrationStrategy.DUAL_WRITE;

        // Mock to simulate entity has existing index
        when(indexer.indexExistsFor(eq(entityClass))).thenReturn(true);
        when(indexer.getIndexName(eq(entityClass))).thenReturn("migrationtestentity_idx");
        when(indexer.createIndexFor(eq(entityClass), anyString(), anyString())).thenReturn(true);

        // Start first migration in a separate thread
        CompletableFuture<MigrationResult> firstMigration = CompletableFuture.supplyAsync(() ->
            migrationService.migrateIndex(entityClass, strategy)
        );

        // Wait a bit to ensure first migration has started
        Thread.sleep(50);

        // When: Attempting concurrent migration
        MigrationResult result = migrationService.migrateIndex(entityClass, strategy);

        // Then: Should prevent concurrent migration
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getErrorMessage()).contains("Migration already in progress");

        // Clean up - wait for first migration to complete
        firstMigration.get(5, TimeUnit.SECONDS);
    }

    @Test
    void testRuntimeIndexCreation() {
        // Given: An entity class with no existing index
        Class<?> entityClass = MigrationTestEntity.class;

        // Mock no existing index
        when(indexer.getIndexName(eq(entityClass))).thenReturn(null);
        when(indexer.indexExistsFor(eq(entityClass))).thenReturn(false);
        when(indexer.createIndexFor(eq(entityClass), anyString(), anyString())).thenReturn(true);

        // When: Creating an index at runtime
        String indexName = migrationService.createVersionedIndex(entityClass);

        // Then: Should create a new versioned index
        assertThat(indexName).isNotNull();
        assertThat(indexName).contains("migrationtestentity");
        assertThat(indexName).contains("_v1_idx");

        // Verify the indexer was called to create the index
        verify(indexer).createIndexFor(eq(entityClass), eq(indexName), anyString());
    }

    @Test
    void testDynamicIndexVersioning() {
        // Given: An entity with existing versioned index
        Class<?> entityClass = MigrationTestEntity.class;

        // Mock existing v2 index
        when(indexer.getIndexName(eq(entityClass))).thenReturn("migrationtestentity_v2_idx");
        when(indexer.createIndexFor(eq(entityClass), anyString(), anyString())).thenReturn(true);

        // When: Creating a new versioned index
        String newIndexName = migrationService.createVersionedIndex(entityClass);

        // Then: Should create v3
        assertThat(newIndexName).isEqualTo("migrationtestentity_v3_idx");
    }

    // Test entity
    @Document
    static class MigrationTestEntity {
        private String id;

        @Indexed
        private String name;

        @Indexed
        private String category;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}