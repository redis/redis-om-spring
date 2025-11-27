package com.redis.om.spring.ops;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.search.SearchOperations;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.search.*;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.schemafields.SchemaField;
import redis.clients.jedis.search.schemafields.TextField;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
class CommandListenerTest extends AbstractBaseDocumentTest {
    @Autowired
    private RedisModulesOperations<String> modulesOperations;

    @Autowired
    private TestCommandListener testListener;

    @BeforeEach
    void setUp() {
      testListener.reset();
    }

    @ParameterizedTest
    @ValueSource(strings = { "test-create-index", "another-create-index", "yet-another-create-index" })
    void givenIndexName_whenCreateAndDropIndex_thenAssertResult(final String indexName) {
        // When
        final SearchOperations<String> searchOps = createIndex(indexName + "-" + UUID.randomUUID());
        // Then
        final String result = searchOps.dropIndex();
        // Then
        assertNotNull(result);
        assertEquals(1, testListener.dropIndexStartedCount.get());
        assertEquals(1, testListener.dropIndexFinishedCount.get());
        assertEquals(indexName, testListener.lastDropIndexName);
        assertEquals(result, testListener.lastDropIndexResult);
    }

    @Test
    void given_whenSearch_thenAssertResult() {
        // Given
        final String indexName = "test-search-index-" + UUID.randomUUID();
        final SearchOperations<String> searchOps = createIndex(indexName + "-" + UUID.randomUUID());
        // When
        final Query query = new Query("*");
        final SearchResult result = searchOps.search(query);
        // Then
        assertEquals(1, testListener.searchStartedCount.get());
        assertEquals(1, testListener.searchFinishedCount.get());
        assertEquals(indexName, testListener.lastSearchIndexName);
        assertEquals(query, testListener.lastSearchQuery);
        assertEquals(result, testListener.lastSearchResult);
        searchOps.dropIndex();
    }

    @Test
    void given_whenAggregate_thenAssertResult() {
        // Given
        final String indexName = "test-aggregate-index-" + UUID.randomUUID();
        final SearchOperations<String> searchOps = createIndex(indexName);
        // When
        final AggregationBuilder aggBuilder = new AggregationBuilder("*");
        final AggregationResult aggResult = searchOps.aggregate(aggBuilder);
        // Then
        assertNotNull(aggResult);
        assertEquals(1, testListener.aggregateStartedCount.get());
        assertEquals(1, testListener.aggregateFinishedCount.get());
        assertEquals(indexName, testListener.lastAggregateIndexName);
        assertEquals(aggBuilder, testListener.lastAggregationBuilder);
    }

    private SearchOperations<String> createIndex(final String indexName) {
        // Given
        final SearchOperations<String> searchOps = modulesOperations.opsForSearch(indexName);
        try {
            searchOps.dropIndex();
        } catch (Exception e) {
            log.warn("Index did not exist prior to test setup: {}", e.getMessage());
        }
        final FTCreateParams params = FTCreateParams.createParams()
                .on(IndexDataType.HASH)
                .prefix(indexName + ":");
        final List<SchemaField> fields = List.of( TextField.of("content"));
        // When
        final String result = searchOps.createIndex(params, fields);
        // Then
        assertThat(testListener.createIndexStartedCount.get()).isEqualTo(1);
        assertThat(testListener.createIndexFinishedCount.get()).isEqualTo(1);
        assertThat(testListener.lastCreateIndexName).isEqualTo(indexName);
        assertThat(testListener.lastCreateParams).isEqualTo(params);
        assertThat(testListener.lastCreateFields).isEqualTo(fields);
        assertThat(testListener.lastCreateResult).isEqualTo(result);
        return searchOps;
    }
}
