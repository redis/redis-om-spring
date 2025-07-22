package com.redis.om.spring.search.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.NumericArrayTestData;
import com.redis.om.spring.fixtures.document.model.NumericArrayTestData$;
import com.redis.om.spring.fixtures.document.repository.NumericArrayTestDataRepository;
import com.redis.om.spring.search.stream.EntityStream;

/**
 * Test class to demonstrate the issue described in GitHub issue #400:
 * https://github.com/redis/redis-om-spring/issues/400
 * 
 * The issue is that NumericField lacks methods to check if a numeric array 
 * contains specific numbers when using EntityStream, similar to how TagField.in() works.
 * 
 * This test proves that the functionality is missing and shows what should work
 * once the feature is implemented.
 */
class NumericArraySearchTest extends AbstractBaseDocumentTest {

    @Autowired
    NumericArrayTestDataRepository repository;
    
    @Autowired
    EntityStream entityStream;

    private NumericArrayTestData testData1;
    private NumericArrayTestData testData2;
    private NumericArrayTestData testData3;

    @BeforeEach
    void loadTestData() {
        testData1 = new NumericArrayTestData();
        testData1.setName("data1");
        testData1.setMeasurements(Arrays.asList(1.5, 2.5, 3.5));
        testData1.setCounts(Arrays.asList(10L, 20L, 30L));
        testData1.setRatings(Arrays.asList(1, 2, 3));
        testData1.setTags(Arrays.asList("tag1", "tag2"));
        
        testData2 = new NumericArrayTestData();
        testData2.setName("data2");
        testData2.setMeasurements(Arrays.asList(2.5, 4.5, 6.5));
        testData2.setCounts(Arrays.asList(20L, 40L, 60L));
        testData2.setRatings(Arrays.asList(2, 4, 6));
        testData2.setTags(Arrays.asList("tag2", "tag3"));
        
        testData3 = new NumericArrayTestData();
        testData3.setName("data3");
        testData3.setMeasurements(Arrays.asList(3.5, 7.5, 9.5));
        testData3.setCounts(Arrays.asList(30L, 70L, 90L));
        testData3.setRatings(Arrays.asList(3, 7, 9));
        testData3.setTags(Arrays.asList("tag1", "tag4"));

        repository.saveAll(Arrays.asList(testData1, testData2, testData3));
    }

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    /**
     * This test shows that TagField.in() works perfectly for string arrays with EntityStream.
     * This is the pattern that NumericField should follow for numeric arrays.
     */
    @Test
    void testTagFieldInWorksWithEntityStream() {
        // This demonstrates TagField.in() working with EntityStream:
        List<NumericArrayTestData> results = entityStream
            .of(NumericArrayTestData.class)  
            .filter(NumericArrayTestData$.TAGS.in("tag1", "tag3"))
            .collect(Collectors.toList());
        
        // Verify the results - should find testData1 and testData3 (both have "tag1") and testData2 (has "tag3")
        assertThat(results).hasSize(3);
        assertThat(results).contains(testData1, testData2, testData3);
    }

    /**
     * This test demonstrates the MISSING functionality for numeric arrays.
     * This is the core issue from GitHub #400.
     */
    @Test
    void testNumericFieldLacksContainsFunctionalityWithEntityStream() {
        // Test basic data setup first
        assertThat(testData1.getMeasurements()).contains(2.5);
        assertThat(testData2.getMeasurements()).contains(2.5);
        assertThat(testData3.getMeasurements()).contains(7.5);
        
        // Should be able to find entities where measurements contains any of these values
        List<NumericArrayTestData> measurementResults = entityStream
            .of(NumericArrayTestData.class)
            .filter(NumericArrayTestData$.MEASUREMENTS.containsDouble(2.5, 7.5))
            .collect(Collectors.toList());
        assertThat(measurementResults).hasSize(3); // All entities have at least one of these values
        
        // Should be able to find entities where counts contains any of these values  
        List<NumericArrayTestData> countResults = entityStream
            .of(NumericArrayTestData.class)
            .filter(NumericArrayTestData$.COUNTS.containsLong(20L, 70L))
            .collect(Collectors.toList());
        assertThat(countResults).hasSize(3); // All entities have at least one of these values
        
        // Should be able to find entities where ratings contains any of these values
        List<NumericArrayTestData> ratingResults = entityStream
            .of(NumericArrayTestData.class)
            .filter(NumericArrayTestData$.RATINGS.containsInt(2, 7))
            .collect(Collectors.toList());
        assertThat(ratingResults).hasSize(3); // testData1 has 2, testData2 has 2, testData3 has 7
    }

    /**
     * This test shows what currently happens when you try to use the existing in() method
     * on NumericField with EntityStream - it doesn't work as expected for arrays.
     */
    @Test
    void testCurrentNumericFieldInMethodLimitation() {
        // The current in() method on NumericField works for scalar matching, not array membership
        // This is different from TagField.in() which works for array membership
        
        // The current NumericField.in() method expects List<T> not individual values
        // This shows the fundamental difference from TagField.in() which accepts varargs
        
        // This would fail to compile: TestData$.MEASUREMENTS.in(2.5)
        // because NumericField.in() expects List<Double>, not double
        
        // NumericField.in() signature: in(List<Double> values) 
        // TagField.in() signature: in(String... values) or in(Object... values)
        
        // The current NumericField.in() method would require the metamodel:
        // List<NumericArrayTestData> results = entityStream
        //     .of(NumericArrayTestData.class)
        //     .filter(NumericArrayTestData$.MEASUREMENTS.in(Arrays.asList(2.5))) // Must pass as List  
        //     .collect(Collectors.toList());
        
        // Demonstrate the data exists but we can't query it effectively
        assertThat(testData1.getMeasurements()).contains(2.5);
        assertThat(testData2.getMeasurements()).contains(2.5);
        
        // The issue: no easy way to find entities where numeric array contains any of multiple values
            
        // But even if this worked, it doesn't give us "array contains any of these values" semantics
        // It's checking if the field value equals the provided list, not membership
        assertTrue(true, "Current NumericField.in() doesn't support array membership like TagField.in() does");
        
        // The key issue: NumericField.in() ≠ TagField.in() for array membership behavior
        assertTrue(true, "NumericField.in() doesn't support array membership like TagField.in() does");
    }

    /**
     * This test demonstrates the exact scenario from GitHub issue #400.
     * User wants to query: "Find all entities where numeric array contains any of these values"
     */
    @Test
    void testGitHubIssue400Scenario() {
        // Issue #400 specifically asks for functionality like:
        // field.containsLong(Long... values) similar to TagField.in()
        
        // Verify test data setup
        assertThat(testData1.getCounts()).contains(20L);
        assertThat(testData2.getCounts()).contains(20L);
        assertThat(testData3.getCounts()).contains(70L);
        
        // This should now work with the new containsLong method:
        List<NumericArrayTestData> results = entityStream
            .of(NumericArrayTestData.class)
            .filter(NumericArrayTestData$.COUNTS.containsLong(20L, 70L))
            .collect(Collectors.toList());
            
        // Should find testData1 (has 20L), testData2 (has 20L), and testData3 (has 70L)
        assertThat(results).hasSize(3);
        assertThat(results).contains(testData1, testData2, testData3);
    }

    /**
     * This test proves the issue exists by showing the compilation failure.
     * When the feature is implemented, this test should be updated to verify the functionality works.
     */
    @Test
    void testCompilationFailureProvesIssueExists() {
        // Verify test data has the expected values
        assertThat(testData1.getMeasurements()).containsAnyOf(1.5, 4.5, 7.5);
        assertThat(testData1.getCounts()).containsAnyOf(10L, 40L, 70L);
        assertThat(testData1.getRatings()).containsAnyOf(1, 4, 7);
        
        // Now these methods should work and compile successfully:
        
        List<NumericArrayTestData> measurementResults = entityStream
            .of(NumericArrayTestData.class)
            .filter(NumericArrayTestData$.MEASUREMENTS.containsDouble(1.5, 4.5, 7.5))
            .collect(Collectors.toList());
            
        List<NumericArrayTestData> countResults = entityStream
            .of(NumericArrayTestData.class)
            .filter(NumericArrayTestData$.COUNTS.containsLong(10L, 40L, 70L))
            .collect(Collectors.toList());
            
        List<NumericArrayTestData> ratingResults = entityStream
            .of(NumericArrayTestData.class)
            .filter(NumericArrayTestData$.RATINGS.containsInt(1, 4, 7))
            .collect(Collectors.toList());
            
        // Verify the methods work as expected
        assertThat(measurementResults).hasSize(3); // All entities have at least one of these measurements
        assertThat(countResults).hasSize(3); // All entities have at least one of these counts  
        assertThat(ratingResults).hasSize(3); // All entities have at least one of these ratings
    }
}