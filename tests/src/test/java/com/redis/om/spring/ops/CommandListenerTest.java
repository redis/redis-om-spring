package com.redis.om.spring.ops;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.search.SearchOperations;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.schemafields.SchemaField;
import redis.clients.jedis.search.schemafields.TextField;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        // Given
        final String idx = indexName + "-" + UUID.randomUUID();
        // When
        final SearchOperations<String> searchOps = createIndex(idx);
        // Then
        final String result = searchOps.dropIndex();
        // Then
        assertNotNull(result);
        assertEquals(2, testListener.dropIndexStartedCount.get());
        assertEquals(2, testListener.dropIndexFinishedCount.get());
        assertEquals(idx, testListener.lastDropIndexName);
        assertEquals(result, testListener.lastDropIndexResult);
    }

    @Test
    void given_whenSearch_thenAssertResult() {
        // Given
        final String indexName = "test-search-index-" + UUID.randomUUID();
        final SearchOperations<String> searchOps = createIndex(indexName);
        // Insert test data
        final String doc1Key = indexName + ":doc1";
        final String doc2Key = indexName + ":doc2";
        final String doc3Key = indexName + ":doc3";
        template.opsForHash().put(doc1Key, "content", "The quick brown fox");
        template.opsForHash().put(doc2Key, "content", "jumps over the lazy dog");
        template.opsForHash().put(doc3Key, "content", "Redis search is powerful");
        // When
        final Query query = new Query("fox");
        final SearchResult result = searchOps.search(query);
        // Then
        assertEquals(1, testListener.searchStartedCount.get());
        assertEquals(1, testListener.searchFinishedCount.get());
        assertEquals(indexName, testListener.lastSearchIndexName);
        assertEquals(query, testListener.lastSearchQuery);
        assertEquals(result, testListener.lastSearchResult);
        assertEquals(1, result.getTotalResults());
        assertNotNull(result.getDocuments());
        assertEquals(1, result.getDocuments().size());
        assertEquals(doc1Key, result.getDocuments().getFirst().getId());
        assertEquals("The quick brown fox", result.getDocuments().getFirst().getString("content"));
        template.delete(doc1Key);
        template.delete(doc2Key);
        template.delete(doc3Key);
    }

    @Test
    void given_whenAggregate_thenAssertResult() {
        // Given
        final String indexName = "test-aggregate-index-" + UUID.randomUUID();
        final SearchOperations<String> searchOps = createIndex(indexName);
        // Insert test data for aggregation
        template.opsForHash().put(indexName + ":doc1", "content", "Java programming");
        template.opsForHash().put(indexName + ":doc2", "content", "Python programming");
        template.opsForHash().put(indexName + ":doc3", "content", "Redis database");
        // When
        final AggregationBuilder aggBuilder = new AggregationBuilder("*");
        final AggregationResult aggResult = searchOps.aggregate(aggBuilder);
        // Then
        assertNotNull(aggResult);
        assertEquals(1, aggResult.getTotalResults());
        assertEquals(1, testListener.aggregateStartedCount.get());
        assertEquals(1, testListener.aggregateFinishedCount.get());
        assertEquals(indexName, testListener.lastAggregateIndexName);
        assertEquals(aggBuilder, testListener.lastAggregationBuilder);
        template.delete(indexName + ":doc1");
        template.delete(indexName + ":doc2");
        template.delete(indexName + ":doc3");
    }

    @Test
    void given_whenGetInfo_thenAssertResult() {
        // Given
        final String indexName = "test-info-index-" + UUID.randomUUID();
        final SearchOperations<String> searchOps = createIndex(indexName);
        // When
        final Map<String, Object> infoResult = searchOps.getInfo();
        // Then
        assertNotNull(infoResult);
        assertThat(infoResult).isNotEmpty();
        assertEquals(1, testListener.infoStartedCount.get());
        assertEquals(1, testListener.infoFinishedCount.get());
    }

    @Test
    void given_whenExplainQuery_thenAssertResult() {
        // Given
        final String indexName = "test-explain-index-" + UUID.randomUUID();
        final SearchOperations<String> searchOps = createIndex(indexName);
        // When
        final Query query = new Query("*");
        final String explanation = searchOps.explain(query);
        // Then
        assertNotNull(explanation);
        assertEquals("<WILDCARD>\n", explanation);
    }

    @Test
    void given_whenAlterIndex_thenAssertResult() {
        // Given
        final String indexName = "test-alter-index-" + UUID.randomUUID();
        final SearchOperations<String> searchOps = createIndex(indexName);
        // When
        final SchemaField newField = TextField.of("newfield");
        final String result = searchOps.alterIndex(newField);
        // Then
        assertEquals("OK", result);
        assertEquals(1, testListener.alterIndexStartedCount.get());
        assertEquals(1, testListener.alterIndexFinishedCount.get());
    }

    @Test
    void given_whenDropIndexAndDocuments_thenAssertResult() {
        // Given
        final String indexName = "test-drop-docs-index-" + UUID.randomUUID();
        final SearchOperations<String> searchOps = createIndex(indexName);
        // Insert test data
        final String doc1Key = indexName + ":doc1";
        final String doc2Key = indexName + ":doc2";
        final String doc3Key = indexName + ":doc3";
        template.opsForHash().put(doc1Key, "content", "The quick brown fox");
        template.opsForHash().put(doc2Key, "content", "jumps over the lazy dog");
        template.opsForHash().put(doc3Key, "content", "Redis search is powerful");
        // When
        final String result = searchOps.dropIndexAndDocuments();
        // Then
        assertEquals("OK", result);
        assertEquals(1, testListener.dropIndexAndDocumentsStartedCount.get());
        assertEquals(1, testListener.dropIndexAndDocumentsFinishedCount.get());
        template.delete(doc1Key);
        template.delete(doc2Key);
        template.delete(doc3Key);
        assertEquals(0, template.opsForHash().keys(doc1Key).size());
        assertEquals(0, template.opsForHash().keys(doc2Key).size());
        assertEquals(0, template.opsForHash().keys(doc3Key).size());
    }

    @Test
    void given_whenAddUpdateDeleteAlias_thenAssertResult() {
        // Given
        final String indexName = "test-add-alias-index-" + UUID.randomUUID();
        final SearchOperations<String> searchOps = createIndex(indexName);
        // Insert test data
        final String doc1Key = indexName + ":doc1";
        final String doc2Key = indexName + ":doc2";
        final String doc3Key = indexName + ":doc3";
        template.opsForHash().put(doc1Key, "content", "The quick brown fox");
        template.opsForHash().put(doc2Key, "content", "jumps over the lazy dog");
        template.opsForHash().put(doc3Key, "content", "Redis search is powerful");
        final String newIndexName = UUID.randomUUID().toString();
        // When addAlias
        String result = searchOps.addAlias(newIndexName);
        // Then addAlias
        assertEquals("OK", result);
        assertEquals(1, testListener.addAliasStartedCount.get());
        assertEquals(1, testListener.addAliasFinishedCount.get());
        // When updateAlias
        result = searchOps.updateAlias(newIndexName);
        // Then updateAlias
        assertEquals("OK", result);
        assertEquals(1, testListener.updateAliasStartedCount.get());
        assertEquals(1, testListener.updateAliasFinishedCount.get());
        // When deleteAlias
        result = searchOps.deleteAlias(newIndexName);
        // Then deleteAlias
        assertEquals("OK", result);
        assertEquals(1, testListener.deleteAliasStartedCount.get());
        assertEquals(1, testListener.deleteAliasFinishedCount.get());
        template.delete(doc1Key);
        template.delete(doc2Key);
        template.delete(doc3Key);
        assertEquals(0, template.opsForHash().keys(doc1Key).size());
        assertEquals(0, template.opsForHash().keys(doc2Key).size());
        assertEquals(0, template.opsForHash().keys(doc3Key).size());
    }

    @Test
    void given_whenGetConfig_thenAssertResult() {
        // Given
        final String indexName = "test-get-config-index-" + UUID.randomUUID();
        final SearchOperations<String> searchOps = createIndex(indexName);
        // When
        final Map<String, Object> result = searchOps.getConfig("*");
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, testListener.getConfigStartedCount.get());
        assertEquals(1, testListener.getConfigFinishedCount.get());
    }

    @Test
    void given_whenGetIndexConfig_thenAssertResult() {
        // Given
        final String indexName = "test-get-config-index-" + UUID.randomUUID();
        final SearchOperations<String> searchOps = createIndex(indexName);
        // When
        final Map<String, Object> result = searchOps.getIndexConfig("*");
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, testListener.getIndexConfigStartedCount.get());
        assertEquals(1, testListener.getIndexConfigFinishedCount.get());
    }

    @Test
    void given_whenSetConfig_thenAssertResult() {
        // Given
        final String indexName = "test-get-config-index-" + UUID.randomUUID();
        final SearchOperations<String> searchOps = createIndex(indexName);
        // When
        final String result = searchOps.setConfig("TIMEOUT", "42");
        // Then
        assertEquals("OK", result);
        assertEquals(1, testListener.setConfigStartedCount.get());
        assertEquals(1, testListener.setConfigFinishedCount.get());
        final Map<String, Object> getTimeout = searchOps.getConfig("TIMEOUT");
        assertEquals("42", getTimeout.get("TIMEOUT").toString());
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
