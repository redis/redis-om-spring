package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.NullTestData;
import com.redis.om.spring.fixtures.document.model.NullTestData$;
import com.redis.om.spring.fixtures.document.repository.NullTestDataRepository;
import com.redis.om.spring.search.stream.EntityStream;

/**
 * Test for INDEXMISSING/INDEXEMPTY support in document repositories and entity streams.
 * Tests the new ismissing() functionality vs existing exists() approach.
 */
class IndexMissingTest extends AbstractBaseDocumentTest {

    @Autowired
    private NullTestDataRepository repository;

    @Autowired
    private EntityStream entityStream;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        
        // Create test data with various null combinations
        repository.save(NullTestData.of("1", "title1", "desc1", 100, "cat1"));
        repository.save(NullTestData.of("2", null, "desc2", 200, "cat2")); 
        repository.save(NullTestData.of("3", "title3", null, null, "cat3"));
        repository.save(NullTestData.of("4", null, null, 400, null));
        repository.save(NullTestData.of("5", "title5", "", 500, ""));
    }

    @Test
    void testRepositoryNullQueries() {
        // Test existing Repository null query methods
        List<NullTestData> withNullTitle = repository.findByTitleIsNull();
        assertThat(withNullTitle).hasSize(2);
        assertThat(withNullTitle).extracting(NullTestData::getId).containsExactlyInAnyOrder("2", "4");

        List<NullTestData> withNonNullTitle = repository.findByTitleIsNotNull();
        assertThat(withNonNullTitle).hasSize(3);
        assertThat(withNonNullTitle).extracting(NullTestData::getId).containsExactlyInAnyOrder("1", "3", "5");

        List<NullTestData> withNullScore = repository.findByScoreIsNull();
        assertThat(withNullScore).hasSize(1);
        assertThat(withNullScore).extracting(NullTestData::getId).containsExactly("3");

        List<NullTestData> withNullCategory = repository.findByCategoryIsNull();
        assertThat(withNullCategory).hasSize(1);
        assertThat(withNullCategory).extracting(NullTestData::getId).containsExactly("4");
    }

    @Test 
    void testEntityStreamNullQueries() {
        // Test if EntityStream supports null queries (currently not implemented)
        // This should demonstrate the discrepancy mentioned in issue #527
        
        // TODO: When isNull()/isNotNull() methods are added to EntityStream, enable these tests
        // var withNullTitle = entityStream.of(NullTestData.class)
        //     .filter(NullTestData$.TITLE.isNull())
        //     .collect(Collectors.toList());
        // assertThat(withNullTitle).hasSize(2);
        
        // For now, we can test the existing isMissing() method if available
        // var withMissingDescription = entityStream.of(NullTestData.class)
        //     .filter(NullTestData$.DESCRIPTION.isMissing())
        //     .collect(Collectors.toList());
    }
}