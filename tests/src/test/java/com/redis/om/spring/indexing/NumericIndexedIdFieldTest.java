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
import com.redis.om.spring.fixtures.document.model.NumericIdTestEntity;
import com.redis.om.spring.fixtures.document.repository.NumericIdTestEntityRepository;

class NumericIndexedIdFieldTest extends AbstractBaseDocumentTest {

    @Autowired
    NumericIdTestEntityRepository repository;

    private NumericIdTestEntity entity1;
    private NumericIdTestEntity entity2;
    private NumericIdTestEntity entity3;

    @BeforeEach
    void setUp() {
        // Create test entities with numeric IDs
        entity1 = new NumericIdTestEntity("Entity One", 100);
        entity1.setId(1001L);
        
        entity2 = new NumericIdTestEntity("Entity Two", 200);
        entity2.setId(1002L);
        
        entity3 = new NumericIdTestEntity("Entity Three", 100);
        entity3.setId(1003L);

        repository.saveAll(Arrays.asList(entity1, entity2, entity3));
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void testNumericIndexedIdFieldDoesNotCauseDuplicateSchema() {
        // This test verifies that having @NumericIndexed on an @Id field
        // doesn't cause duplicate schema field errors
        
        // If the fix in RediSearchIndexer is working correctly,
        // the entities should be saved and queryable without issues
        
        // Verify entities were saved
        assertThat(repository.count()).isEqualTo(3);
        
        // Verify we can query by ID
        assertThat(repository.findById(1001L)).isPresent();
        assertThat(repository.findById(1002L)).isPresent();
        assertThat(repository.findById(1003L)).isPresent();
    }

    @Test
    void testFindByIdIn() {
        List<Long> ids = Arrays.asList(1001L, 1003L);
        List<NumericIdTestEntity> results = repository.findByIdIn(ids);
        
        assertThat(results).hasSize(2);
        assertThat(results).contains(entity1, entity3);
        assertThat(results).doesNotContain(entity2);
    }

    @Test
    void testFindByIdNotIn() {
        List<Long> ids = Arrays.asList(1001L, 1002L);
        List<NumericIdTestEntity> results = repository.findByIdNotIn(ids);
        
        assertThat(results).hasSize(1);
        assertThat(results).contains(entity3);
        assertThat(results).doesNotContain(entity1, entity2);
    }

    @Test
    void testFindByIdInAndName() {
        List<Long> ids = Arrays.asList(1001L, 1002L, 1003L);
        List<NumericIdTestEntity> results = repository.findByIdInAndName(ids, "Entity Two");
        
        assertThat(results).hasSize(1);
        assertThat(results).contains(entity2);
    }

    @Test
    void testFindByValueIn() {
        List<Integer> values = Arrays.asList(100, 300);
        List<NumericIdTestEntity> results = repository.findByValueIn(values);
        
        // entity1 and entity3 have value=100
        assertThat(results).hasSize(2);
        assertThat(results).contains(entity1, entity3);
        assertThat(results).doesNotContain(entity2);
    }

    @Test
    void testNumericIdWithLargeValues() {
        // Test with larger ID values
        NumericIdTestEntity largeIdEntity = new NumericIdTestEntity("Large ID Entity", 500);
        largeIdEntity.setId(999999999L);
        repository.save(largeIdEntity);
        
        List<Long> ids = Arrays.asList(999999999L, 1001L);
        List<NumericIdTestEntity> results = repository.findByIdIn(ids);
        
        assertThat(results).hasSize(2);
        assertThat(results).contains(largeIdEntity, entity1);
        
        repository.delete(largeIdEntity);
    }

    @Test
    void testNumericIdQueryPerformance() {
        // Create more entities for performance testing
        for (long i = 2000L; i < 2100L; i++) {
            NumericIdTestEntity entity = new NumericIdTestEntity("Entity " + i, (int)(i % 10));
            entity.setId(i);
            repository.save(entity);
        }
        
        // Query with a large set of IDs
        List<Long> ids = Arrays.asList(1001L, 1002L, 1003L, 2001L, 2050L, 2099L);
        List<NumericIdTestEntity> results = repository.findByIdIn(ids);
        
        assertThat(results).hasSize(6);
        
        // Clean up additional entities
        for (long i = 2000L; i < 2100L; i++) {
            repository.deleteById(i);
        }
    }
}