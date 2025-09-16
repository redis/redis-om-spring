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
import static org.junit.jupiter.api.Assertions.assertAll;

class EntityStreamMissingFieldTest extends AbstractBaseDocumentTest {

    @Autowired
    private RxDocumentRepository rxDocumentRepository;

    @Autowired
    private EntityStream entityStream;

    @BeforeEach
    void beforeEach() {
        rxDocumentRepository.deleteAll();

        // Create test data with various lock states
        RxDocument rx1 = RxDocument.builder()
                .rxNumber("RX001")
                .lock("LOCKED")
                .status("ACTIVE")
                .build();

        RxDocument rx2 = RxDocument.builder()
                .rxNumber("RX002")
                .lock("")  // Empty string
                .status("ACTIVE")
                .build();

        RxDocument rx3 = RxDocument.builder()
                .rxNumber("RX003")
                .lock(null)  // Null value (will be missing in Redis)
                .status("ACTIVE")
                .build();

        RxDocument rx4 = RxDocument.builder()
                .rxNumber("RX004")
                .lock("PROCESSING")
                .status("ACTIVE")
                .build();

        RxDocument rx5 = RxDocument.builder()
                .rxNumber("RX005")
                // lock field not set at all (will be missing)
                .status("INACTIVE")
                .build();

        rxDocumentRepository.saveAll(List.of(rx1, rx2, rx3, rx4, rx5));
    }

    @Test
    void testIsMissingNegateProducesWildcardQuery_ReproducesIssue() {
        // This test reproduces the issue where isMissing().negate() produces "*" query
        // instead of the proper filter "-ismissing(@lock)"
        // Capture the actual query that gets executed
        String indexName = RxDocument.class.getName() + "Idx";

        // Try using isMissing().negate() as user reported
        List<String> results = entityStream.of(RxDocument.class)
                .filter(RxDocument$.LOCK.isMissing().negate())
                .map(RxDocument$.RX_NUMBER)
                .collect(Collectors.toList());

        System.out.println("Results from isMissing().negate(): " + results);

        // After fix: should return RX001, RX002, RX004
        // RX001: lock = "LOCKED" (not missing)
        // RX002: lock = "" (not missing, just empty)
        // RX004: lock = "PROCESSING" (not missing)
        // RX003 and RX005 have null/missing lock fields and should be filtered out

        assertThat(results).as("isMissing().negate() should filter out documents with missing lock field")
                .containsExactlyInAnyOrder("RX001", "RX002", "RX004");
    }

    @Test
    void testIsMissingAlone() {
        // Test that isMissing() works correctly on its own
        List<String> results = entityStream.of(RxDocument.class)
                .filter(RxDocument$.LOCK.isMissing())
                .map(RxDocument$.RX_NUMBER)
                .collect(Collectors.toList());

        // Should return RX003 and RX005 (where lock is null/missing)
        assertThat(results).as("isMissing() should find documents with missing lock field")
                .containsExactlyInAnyOrder("RX003", "RX005");
    }

    @Test
    void testNotEmptyQuery() {
        // Test filtering for non-empty lock values
        List<String> results = entityStream.of(RxDocument.class)
                .filter(RxDocument$.LOCK.notEq(""))
                .map(RxDocument$.RX_NUMBER)
                .collect(Collectors.toList());

        // Should return documents where lock is not an empty string
        // This includes RX001, RX003, RX004, RX005 (excludes only RX002 with empty string)
        assertThat(results).as("notEq('') should filter out only empty strings")
                .containsExactlyInAnyOrder("RX001", "RX003", "RX004", "RX005");
    }

    @Test
    void testCombinedFiltersWithNegatedMissing() {
        // Test combining multiple filters with negated missing
        List<String> results = entityStream.of(RxDocument.class)
                .filter(RxDocument$.STATUS.eq("ACTIVE"))
                .filter(RxDocument$.LOCK.isMissing().negate())
                .map(RxDocument$.RX_NUMBER)
                .collect(Collectors.toList());

        // Should return only ACTIVE documents with non-missing lock
        // RX001, RX002, RX004 are ACTIVE and have non-missing lock
        assertThat(results).as("Combined filter should work correctly")
                .containsExactlyInAnyOrder("RX001", "RX002", "RX004");
    }

    @Test
    void testDoubleNegation() {
        // Test that double negation returns to original
        List<String> results = entityStream.of(RxDocument.class)
                .filter(RxDocument$.LOCK.isMissing().negate().negate())
                .map(RxDocument$.RX_NUMBER)
                .collect(Collectors.toList());

        // Double negation should return to original isMissing()
        assertThat(results).as("Double negation should return to original predicate")
                .containsExactlyInAnyOrder("RX003", "RX005");
    }

    @Test
    void testFindNonEmptyAndNonMissingValues() {
        // Test finding values that are both not empty AND not missing

        // This combines two conditions: not missing AND not empty
        List<String> results = entityStream.of(RxDocument.class)
                .filter(RxDocument$.LOCK.isMissing().negate())
                .filter(RxDocument$.LOCK.notEq(""))
                .map(RxDocument$.RX_NUMBER)
                .collect(Collectors.toList());

        // Should return only RX001 and RX004 (not missing AND not empty)
        assertThat(results).as("Should find only non-empty and non-missing values")
                .containsExactlyInAnyOrder("RX001", "RX004");
    }
}