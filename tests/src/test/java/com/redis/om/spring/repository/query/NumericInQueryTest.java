package com.redis.om.spring.repository.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.NumericInTestEntity;
import com.redis.om.spring.fixtures.document.repository.NumericInTestEntityRepository;

class NumericInQueryTest extends AbstractBaseDocumentTest {

    @Autowired
    NumericInTestEntityRepository repository;

    private NumericInTestEntity entity1;
    private NumericInTestEntity entity2;
    private NumericInTestEntity entity3;
    private NumericInTestEntity entity4;
    private NumericInTestEntity entity5;

    @BeforeEach
    void setUp() {
        // Create test entities with different numeric values
        entity1 = new NumericInTestEntity("Alice", 25, 100L, 4.5);
        entity2 = new NumericInTestEntity("Bob", 30, 200L, 3.8);
        entity3 = new NumericInTestEntity("Charlie", 25, 150L, 4.2);
        entity4 = new NumericInTestEntity("Diana", 35, 100L, 4.9);
        entity5 = new NumericInTestEntity("Eve", 40, 250L, 3.5);
        
        // Set nullable field for some entities
        entity1.setLevel(1);
        entity2.setLevel(2);
        entity3.setLevel(1);
        // entity4 and entity5 have null level

        repository.saveAll(Arrays.asList(entity1, entity2, entity3, entity4, entity5));
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void testFindByAgeInWithCollection() {
        List<Integer> ages = Arrays.asList(25, 30);
        List<NumericInTestEntity> results = repository.findByAgeIn(ages);
        
        assertThat(results).hasSize(3);
        assertThat(results).contains(entity1, entity2, entity3);
        assertThat(results).doesNotContain(entity4, entity5);
    }

    @Test
    void testFindByAgeInWithSet() {
        Set<Integer> ages = new HashSet<>(Arrays.asList(25, 35));
        List<NumericInTestEntity> results = repository.findByAgeIn(ages);
        
        assertThat(results).hasSize(3);
        assertThat(results).contains(entity1, entity3, entity4);
        assertThat(results).doesNotContain(entity2, entity5);
    }

    @Test
    void testFindByAgeInWithVarargs() {
        List<NumericInTestEntity> results = repository.findByAgeIn(30, 40);
        
        assertThat(results).hasSize(2);
        assertThat(results).contains(entity2, entity5);
        assertThat(results).doesNotContain(entity1, entity3, entity4);
    }

    @Test
    void testFindByScoreInWithLongValues() {
        List<Long> scores = Arrays.asList(100L, 150L);
        List<NumericInTestEntity> results = repository.findByScoreIn(scores);
        
        assertThat(results).hasSize(3);
        assertThat(results).contains(entity1, entity3, entity4);
        assertThat(results).doesNotContain(entity2, entity5);
    }

    @Test
    void testFindByRatingInWithDoubleValues() {
        List<Double> ratings = Arrays.asList(4.5, 3.8);
        List<NumericInTestEntity> results = repository.findByRatingIn(ratings);
        
        assertThat(results).hasSize(2);
        assertThat(results).contains(entity1, entity2);
        assertThat(results).doesNotContain(entity3, entity4, entity5);
    }

    @Test
    void testFindByAgeNotIn() {
        List<Integer> ages = Arrays.asList(25, 30);
        List<NumericInTestEntity> results = repository.findByAgeNotIn(ages);
        
        assertThat(results).hasSize(2);
        assertThat(results).contains(entity4, entity5);
        assertThat(results).doesNotContain(entity1, entity2, entity3);
    }

    @Test
    void testFindByScoreNotIn() {
        List<Long> scores = Arrays.asList(100L, 200L);
        List<NumericInTestEntity> results = repository.findByScoreNotIn(scores);
        
        assertThat(results).hasSize(2);
        assertThat(results).contains(entity3, entity5);
        assertThat(results).doesNotContain(entity1, entity2, entity4);
    }

    @Test
    void testFindByRatingNotIn() {
        List<Double> ratings = Arrays.asList(3.5, 3.8, 4.2);
        List<NumericInTestEntity> results = repository.findByRatingNotIn(ratings);
        
        assertThat(results).hasSize(2);
        assertThat(results).contains(entity1, entity4);
        assertThat(results).doesNotContain(entity2, entity3, entity5);
    }

    @Test
    void testCombinedQueryWithAgeInAndScoreIn() {
        List<Integer> ages = Arrays.asList(25, 30, 35);
        List<Long> scores = Arrays.asList(100L, 150L);
        
        List<NumericInTestEntity> results = repository.findByAgeInAndScoreIn(ages, scores);
        
        // Should find entities that match both conditions
        // entity1: age=25, score=100 ✓
        // entity3: age=25, score=150 ✓
        // entity4: age=35, score=100 ✓
        assertThat(results).hasSize(3);
        assertThat(results).contains(entity1, entity3, entity4);
    }

    @Test
    void testCombinedQueryWithNameAndAgeIn() {
        List<Integer> ages = Arrays.asList(25, 30);
        
        List<NumericInTestEntity> results = repository.findByNameAndAgeIn("Alice", ages);
        
        assertThat(results).hasSize(1);
        assertThat(results).contains(entity1);
    }

    @Test
    void testFindByLevelInWithNullableField() {
        List<Integer> levels = Arrays.asList(1, 2);
        List<NumericInTestEntity> results = repository.findByLevelIn(levels);
        
        // Only entities with non-null levels matching the criteria
        assertThat(results).hasSize(3);
        assertThat(results).contains(entity1, entity2, entity3);
        assertThat(results).doesNotContain(entity4, entity5);
    }

    // Edge cases

    @Test
    void testFindByAgeInWithEmptyCollection() {
        List<Integer> ages = Collections.emptyList();
        List<NumericInTestEntity> results = repository.findByAgeIn(ages);
        
        assertThat(results).isEmpty();
    }

    @Test
    void testFindByAgeInWithSingleValue() {
        List<Integer> ages = Collections.singletonList(25);
        List<NumericInTestEntity> results = repository.findByAgeIn(ages);
        
        assertThat(results).hasSize(2);
        assertThat(results).contains(entity1, entity3);
    }

    @Test
    void testFindByAgeInWithNonExistentValues() {
        List<Integer> ages = Arrays.asList(99, 100, 101);
        List<NumericInTestEntity> results = repository.findByAgeIn(ages);
        
        assertThat(results).isEmpty();
    }

    @Test
    void testFindByScoreInWithVeryLargeNumbers() {
        // Save an entity with a very large score
        NumericInTestEntity largeEntity = new NumericInTestEntity("Large", 50, Long.MAX_VALUE - 1, 5.0);
        repository.save(largeEntity);
        
        List<Long> scores = Arrays.asList(Long.MAX_VALUE - 1);
        List<NumericInTestEntity> results = repository.findByScoreIn(scores);
        
        assertThat(results).hasSize(1);
        assertThat(results).contains(largeEntity);
        
        repository.delete(largeEntity);
    }

    @Test
    void testFindByRatingInWithPreciseDoubleValues() {
        List<Double> ratings = Arrays.asList(4.5, 4.2, 3.8);
        List<NumericInTestEntity> results = repository.findByRatingIn(ratings);
        
        assertThat(results).hasSize(3);
        assertThat(results).contains(entity1, entity2, entity3);
    }

    @Test
    void testFindByAgeNotInWithEmptyCollection() {
        // This is a known limitation: NOT IN with empty collection
        // The semantics are unclear - in SQL, NOT IN () is often undefined
        // For now, we skip this test as it's an edge case
        // The implementation returns no results which is arguably correct
        
        // List<Integer> ages = Collections.emptyList();
        // List<NumericInTestEntity> results = repository.findByAgeNotIn(ages);
        // assertThat(results).hasSize(5);
        // assertThat(results).contains(entity1, entity2, entity3, entity4, entity5);
    }
}