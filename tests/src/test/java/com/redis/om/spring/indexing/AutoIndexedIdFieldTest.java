package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.AutoIndexedIdEntity;
import com.redis.om.spring.fixtures.document.repository.AutoIndexedIdEntityRepository;

/**
 * Tests for auto-indexed @Id fields (without explicit @NumericIndexed annotation).
 *
 * This tests GitHub issue #699 - findByXXXIn should work with auto-indexed ID fields.
 * The ID field with Long type should be automatically indexed as NUMERIC,
 * and findByIdIn queries should work without requiring explicit @NumericIndexed.
 */
class AutoIndexedIdFieldTest extends AbstractBaseDocumentTest {

    @Autowired
    AutoIndexedIdEntityRepository repository;

    private AutoIndexedIdEntity entity1;
    private AutoIndexedIdEntity entity2;
    private AutoIndexedIdEntity entity3;

    @BeforeEach
    void setUp() {
        // Create test entities with numeric IDs (no @NumericIndexed on ID field)
        entity1 = new AutoIndexedIdEntity("Entity One", 100);
        entity1.setId(1001L);

        entity2 = new AutoIndexedIdEntity("Entity Two", 200);
        entity2.setId(1002L);

        entity3 = new AutoIndexedIdEntity("Entity Three", 100);
        entity3.setId(1003L);

        repository.saveAll(Arrays.asList(entity1, entity2, entity3));
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void testAutoIndexedIdFieldDoesNotCauseDuplicateSchema() {
        // Verify that having just @Id on a Long field doesn't cause issues
        // The entities should be saved and queryable without issues

        // Verify entities were saved
        assertThat(repository.count()).isEqualTo(3);

        // Verify we can query by ID using standard findById
        assertThat(repository.findById(1001L)).isPresent();
        assertThat(repository.findById(1002L)).isPresent();
        assertThat(repository.findById(1003L)).isPresent();
    }

    @Test
    void testFindByIdInWithAutoIndexedId() {
        // This is the key test for issue #699
        // findByIdIn should work with auto-indexed ID fields (no @NumericIndexed needed)
        List<Long> ids = Arrays.asList(1001L, 1003L);
        List<AutoIndexedIdEntity> results = repository.findByIdIn(ids);

        assertThat(results).hasSize(2);
        assertThat(results).contains(entity1, entity3);
        assertThat(results).doesNotContain(entity2);
    }

    @Test
    void testFindByIdNotInWithAutoIndexedId() {
        List<Long> ids = Arrays.asList(1001L, 1002L);
        List<AutoIndexedIdEntity> results = repository.findByIdNotIn(ids);

        assertThat(results).hasSize(1);
        assertThat(results).contains(entity3);
        assertThat(results).doesNotContain(entity1, entity2);
    }

    @Test
    void testFindByIdInAndNameWithAutoIndexedId() {
        List<Long> ids = Arrays.asList(1001L, 1002L, 1003L);
        List<AutoIndexedIdEntity> results = repository.findByIdInAndName(ids, "Entity Two");

        assertThat(results).hasSize(1);
        assertThat(results).contains(entity2);
    }

    @Test
    void testFindByIdInWithEmptyCollection() {
        // Edge case: empty collection should return empty results
        List<Long> ids = List.of();
        List<AutoIndexedIdEntity> results = repository.findByIdIn(ids);

        assertThat(results).isEmpty();
    }

    @Test
    void testFindByIdInWithSingleId() {
        // Single ID in collection
        List<Long> ids = List.of(1002L);
        List<AutoIndexedIdEntity> results = repository.findByIdIn(ids);

        assertThat(results).hasSize(1);
        assertThat(results).contains(entity2);
    }

    @Test
    void testFindByIdInWithNonExistentIds() {
        // IDs that don't exist
        List<Long> ids = Arrays.asList(9999L, 8888L);
        List<AutoIndexedIdEntity> results = repository.findByIdIn(ids);

        assertThat(results).isEmpty();
    }
}
