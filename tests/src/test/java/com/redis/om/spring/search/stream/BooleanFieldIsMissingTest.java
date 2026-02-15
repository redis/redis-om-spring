package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.RxDocument;
import com.redis.om.spring.fixtures.document.model.RxDocument$;
import com.redis.om.spring.fixtures.document.repository.RxDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class to verify that isMissing() predicate works correctly with fields
 * in the streams API. This demonstrates that fields (including booleans when using
 * String representation) can be filtered not only by specific values but also by
 * null/missing values using the isMissing() method.
 */
class BooleanFieldIsMissingTest extends AbstractBaseDocumentTest {

    @Autowired
    private RxDocumentRepository repository;

    @Autowired
    private EntityStream entityStream;

    @BeforeEach
    void beforeEach() {
        repository.deleteAll();

        // Create test data with various field states
        // Using RxDocument which has a lock field that can be null/missing
        RxDocument doc1 = RxDocument.builder()
                .rxNumber("RX001")
                .lock("LOCKED")  // Has a value
                .status("ACTIVE")
                .build();

        RxDocument doc2 = RxDocument.builder()
                .rxNumber("RX002")
                .lock("UNLOCKED")  // Has a value
                .status("ACTIVE")
                .build();

        RxDocument doc3 = RxDocument.builder()
                .rxNumber("RX003")
                .lock(null)  // Null value (will be missing in Redis)
                .status("ACTIVE")
                .build();

        RxDocument doc4 = RxDocument.builder()
                .rxNumber("RX004")
                // lock field not set at all (will be missing)
                .status("INACTIVE")
                .build();

        repository.saveAll(List.of(doc1, doc2, doc3, doc4));
    }

    @Test
    void testFilterFieldBySpecificValue() {
        // Test filtering by specific value
        List<String> results = entityStream.of(RxDocument.class)
                .filter(RxDocument$.LOCK.eq("LOCKED"))
                .map(RxDocument::getRxNumber)
                .collect(Collectors.toList());

        assertThat(results).as("Should find documents where lock is LOCKED")
                .containsExactly("RX001");
    }

    @Test
    void testFilterFieldByAnotherValue() {
        // Test filtering by another value
        List<String> results = entityStream.of(RxDocument.class)
                .filter(RxDocument$.LOCK.eq("UNLOCKED"))
                .map(RxDocument::getRxNumber)
                .collect(Collectors.toList());

        assertThat(results).as("Should find documents where lock is UNLOCKED")
                .containsExactly("RX002");
    }

    @Test
    void testFilterFieldByIsMissing() {
        // Test filtering by missing/null value using isMissing()
        List<String> results = entityStream.of(RxDocument.class)
                .filter(RxDocument$.LOCK.isMissing())
                .map(RxDocument::getRxNumber)
                .collect(Collectors.toList());

        // Should return RX003 and RX004 (where lock is null/missing)
        assertThat(results).as("isMissing() should find documents with null/missing lock field")
                .hasSize(2)
                .containsExactlyInAnyOrder("RX003", "RX004");
    }

    @Test
    void testFilterFieldByIsNotMissing() {
        // Test filtering by NOT missing using isMissing().negate()
        List<String> results = entityStream.of(RxDocument.class)
                .filter(RxDocument$.LOCK.isMissing().negate())
                .map(RxDocument::getRxNumber)
                .collect(Collectors.toList());

        // Should return RX001 and RX002 (where lock has a value)
        assertThat(results).as("isMissing().negate() should find documents with non-null lock field")
                .hasSize(2)
                .containsExactlyInAnyOrder("RX001", "RX002");
    }

    @Test
    void testCombineIsMissingWithOtherFilters() {
        // Test combining isMissing with other filters
        RxDocument doc5 = RxDocument.builder()
                .rxNumber("RX005")
                .lock("LOCKED")
                .status("ACTIVE")
                .build();
        repository.save(doc5);

        List<String> results = entityStream.of(RxDocument.class)
                .filter(RxDocument$.LOCK.isMissing().negate())
                .filter(RxDocument$.STATUS.eq("ACTIVE"))
                .map(RxDocument::getRxNumber)
                .collect(Collectors.toList());

        // Should find documents where lock is not missing AND status is ACTIVE
        assertThat(results).as("Combined filters should work correctly")
                .hasSize(3)
                .containsExactlyInAnyOrder("RX001", "RX002", "RX005");
    }

    @Test
    void testFilterLockedOrMissing() {
        // Test finding documents where lock is either LOCKED OR missing
        List<String> results = entityStream.of(RxDocument.class)
                .filter(RxDocument$.LOCK.eq("LOCKED")
                        .or(RxDocument$.LOCK.isMissing()))
                .map(RxDocument::getRxNumber)
                .collect(Collectors.toList());

        // Should return RX001 (LOCKED), RX003 (null), and RX004 (missing)
        assertThat(results).as("Should find documents where lock is LOCKED OR missing")
                .hasSize(3)
                .containsExactlyInAnyOrder("RX001", "RX003", "RX004");
    }

    @Test
    void testFilterUnlockedOrMissing() {
        // Test finding documents where lock is either UNLOCKED OR missing
        List<String> results = entityStream.of(RxDocument.class)
                .filter(RxDocument$.LOCK.eq("UNLOCKED")
                        .or(RxDocument$.LOCK.isMissing()))
                .map(RxDocument::getRxNumber)
                .collect(Collectors.toList());

        // Should return RX002 (UNLOCKED), RX003 (null), and RX004 (missing)
        assertThat(results).as("Should find documents where lock is UNLOCKED OR missing")
                .hasSize(3)
                .containsExactlyInAnyOrder("RX002", "RX003", "RX004");
    }

    /**
     * This test reproduces the scenario from EventSessionService where multiple filters
     * are applied in sequence, including an OR condition with isMissing().
     *
     * The pattern being tested:
     * stream.filter(condition1)
     *       .filter(condition2)
     *       .filter(condition3.or(field.isMissing()))
     *
     * This verifies that isMissing() doesn't override previous filters.
     */
    @Test
    void testMultipleFiltersWithIsMissingOrCondition() {
        // Setup: Create a more complex dataset similar to EventSession scenario
        repository.deleteAll();

        // Documents that should match all conditions (status=ACTIVE, lock=false or missing)
        RxDocument doc1 = RxDocument.builder()
                .rxNumber("DOC001")
                .status("ACTIVE")
                .lock("false")  // Explicitly false
                .build();

        RxDocument doc2 = RxDocument.builder()
                .rxNumber("DOC002")
                .status("ACTIVE")
                .lock(null)  // Missing lock field
                .build();

        // Documents that should NOT match (wrong status)
        RxDocument doc3 = RxDocument.builder()
                .rxNumber("DOC003")
                .status("INACTIVE")
                .lock("false")  // Has false but wrong status
                .build();

        RxDocument doc4 = RxDocument.builder()
                .rxNumber("DOC004")
                .status("INACTIVE")
                .lock(null)  // Missing lock but wrong status
                .build();

        // Documents that should NOT match (lock=true)
        RxDocument doc5 = RxDocument.builder()
                .rxNumber("DOC005")
                .status("ACTIVE")
                .lock("true")  // Has true, should be filtered out
                .build();

        repository.saveAll(List.of(doc1, doc2, doc3, doc4, doc5));

        // Execute: Apply multiple filters in sequence, mimicking EventSessionService pattern
        List<String> results = entityStream.of(RxDocument.class)
                .filter(RxDocument$.STATUS.eq("ACTIVE"))  // First filter: status must be ACTIVE
                .filter(RxDocument$.LOCK.eq("false").or(RxDocument$.LOCK.isMissing()))  // Second filter: lock=false OR missing
                .map(RxDocument::getRxNumber)
                .collect(Collectors.toList());

        // Verify: Should only return DOC001 and DOC002
        // DOC003 and DOC004 are excluded because status != ACTIVE
        // DOC005 is excluded because lock = true
        assertThat(results)
                .as("Should respect all filters: status=ACTIVE AND (lock=false OR lock is missing)")
                .hasSize(2)
                .containsExactlyInAnyOrder("DOC001", "DOC002");
    }

    /**
     * Test the exact pattern from the user's code:
     * Multiple filters applied sequentially where the last filter uses OR with isMissing()
     */
    @Test
    void testSequentialFiltersWithFinalOrIsMissing() {
        repository.deleteAll();

        // Create documents with various combinations
        RxDocument doc1 = RxDocument.builder()
                .rxNumber("SEQ001")
                .status("ACTIVE")
                .lock("false")
                .build();

        RxDocument doc2 = RxDocument.builder()
                .rxNumber("SEQ002")
                .status("ACTIVE")
                .lock(null)  // Missing
                .build();

        RxDocument doc3 = RxDocument.builder()
                .rxNumber("SEQ003")
                .status("INACTIVE")
                .lock("false")
                .build();

        RxDocument doc4 = RxDocument.builder()
                .rxNumber("SEQ004")
                .status("INACTIVE")
                .lock(null)  // Missing but wrong status
                .build();

        RxDocument doc5 = RxDocument.builder()
                .rxNumber("SEQ005")
                .status("ACTIVE")
                .lock("true")
                .build();

        repository.saveAll(List.of(doc1, doc2, doc3, doc4, doc5));

        // Test 1: Without the isMissing filter (baseline)
        List<String> resultsWithoutIsMissing = entityStream.of(RxDocument.class)
                .filter(RxDocument$.STATUS.eq("ACTIVE"))
                .map(RxDocument::getRxNumber)
                .collect(Collectors.toList());

        assertThat(resultsWithoutIsMissing)
                .as("Baseline: status=ACTIVE should return 3 documents")
                .hasSize(3)
                .containsExactlyInAnyOrder("SEQ001", "SEQ002", "SEQ005");

        // Test 2: With the isMissing OR condition added
        List<String> resultsWithIsMissing = entityStream.of(RxDocument.class)
                .filter(RxDocument$.STATUS.eq("ACTIVE"))
                .filter(RxDocument$.LOCK.eq("false").or(RxDocument$.LOCK.isMissing()))
                .map(RxDocument::getRxNumber)
                .collect(Collectors.toList());

        assertThat(resultsWithIsMissing)
                .as("With isMissing: should still respect status=ACTIVE filter and only add lock condition")
                .hasSize(2)
                .containsExactlyInAnyOrder("SEQ001", "SEQ002");

        // Test 3: Verify that isMissing alone doesn't return all missing documents
        List<String> onlyMissing = entityStream.of(RxDocument.class)
                .filter(RxDocument$.STATUS.eq("ACTIVE"))
                .filter(RxDocument$.LOCK.isMissing())
                .map(RxDocument::getRxNumber)
                .collect(Collectors.toList());

        assertThat(onlyMissing)
                .as("isMissing with status filter should only return ACTIVE documents with missing lock")
                .hasSize(1)
                .containsExactly("SEQ002");
    }

    /**
     * Test to verify that isMissing() doesn't cause other filters to be ignored
     * when used in a conditional filter application (like the showDismissed flag scenario)
     */
    @Test
    void testConditionalFilterWithIsMissing() {
        repository.deleteAll();

        RxDocument doc1 = RxDocument.builder()
                .rxNumber("COND001")
                .status("ACTIVE")
                .lock("false")
                .build();

        RxDocument doc2 = RxDocument.builder()
                .rxNumber("COND002")
                .status("ACTIVE")
                .lock(null)
                .build();

        RxDocument doc3 = RxDocument.builder()
                .rxNumber("COND003")
                .status("INACTIVE")
                .lock(null)
                .build();

        repository.saveAll(List.of(doc1, doc2, doc3));

        // Simulate the conditional filter pattern: if (!showDismissed) { apply filter }
        boolean showDismissed = false;

        var stream = entityStream.of(RxDocument.class)
                .filter(RxDocument$.STATUS.eq("ACTIVE"));

        if (!showDismissed) {
            stream = stream.filter(RxDocument$.LOCK.eq("false").or(RxDocument$.LOCK.isMissing()));
        }

        List<String> results = stream
                .map(RxDocument::getRxNumber)
                .collect(Collectors.toList());

        assertThat(results)
                .as("Conditional filter should work correctly with isMissing")
                .hasSize(2)
                .containsExactlyInAnyOrder("COND001", "COND002");

        // Now test with showDismissed = true (filter not applied)
        showDismissed = true;
        var stream2 = entityStream.of(RxDocument.class)
                .filter(RxDocument$.STATUS.eq("ACTIVE"));

        if (!showDismissed) {
            stream2 = stream2.filter(RxDocument$.LOCK.eq("false").or(RxDocument$.LOCK.isMissing()));
        }

        List<String> results2 = stream2
                .map(RxDocument::getRxNumber)
                .collect(Collectors.toList());

        assertThat(results2)
                .as("Without the isMissing filter, should return all ACTIVE documents")
                .hasSize(2)
                .containsExactlyInAnyOrder("COND001", "COND002");
    }

}

