package com.redis.om.spring.ops;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.FTSearchParams;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.schemafields.SchemaField;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
* Test implementation of CommandListener that tracks all invocations
*/
@Component
@Primary
class TestCommandListener implements CommandListener {
    // Search tracking
    AtomicInteger searchStartedCount = new AtomicInteger(0);
    AtomicInteger searchFinishedCount = new AtomicInteger(0);
    String lastSearchIndexName;
    Query lastSearchQuery;
    FTSearchParams lastSearchParams;
    SearchResult lastSearchResult;

    // Create index tracking
    AtomicInteger createIndexStartedCount = new AtomicInteger(0);
    AtomicInteger createIndexFinishedCount = new AtomicInteger(0);
    String lastCreateIndexName;
    FTCreateParams lastCreateParams;
    List<SchemaField> lastCreateFields;
    Schema lastCreateSchema;
    IndexOptions lastCreateOptions;
    String lastCreateResult;

    // Aggregate tracking
    AtomicInteger aggregateStartedCount = new AtomicInteger(0);
    AtomicInteger aggregateFinishedCount = new AtomicInteger(0);
    String lastAggregateIndexName;
    AggregationBuilder lastAggregationBuilder;

    // Drop index tracking
    AtomicInteger dropIndexStartedCount = new AtomicInteger(0);
    AtomicInteger dropIndexFinishedCount = new AtomicInteger(0);
    String lastDropIndexName;
    String lastDropIndexResult;

    // Info tracking
    AtomicInteger infoStartedCount = new AtomicInteger(0);
    AtomicInteger infoFinishedCount = new AtomicInteger(0);

    @Override
    public void searchStarted(String indexName, Query q, FTSearchParams params) {
      searchStartedCount.incrementAndGet();
      lastSearchIndexName = indexName;
      lastSearchQuery = q;
      lastSearchParams = params;
    }

    @Override
    public void searchFinished(String indexName, Query q, FTSearchParams params, SearchResult searchResult) {
      searchFinishedCount.incrementAndGet();
      lastSearchResult = searchResult;
    }

    @Override
    public void createIndexStarted(String indexName, FTCreateParams params, List<SchemaField> fields,
                                   Schema schema, IndexOptions options) {
      createIndexStartedCount.incrementAndGet();
      lastCreateIndexName = indexName;
      lastCreateParams = params;
      lastCreateFields = fields;
      lastCreateSchema = schema;
      lastCreateOptions = options;
    }

    @Override
    public void createIndexFinished(String indexName, FTCreateParams params, List<SchemaField> fields,
                                    Schema schema, IndexOptions options, String result) {
      createIndexFinishedCount.incrementAndGet();
      lastCreateResult = result;
    }

    @Override
    public void aggregateStarted(String indexName, AggregationBuilder q) {
      aggregateStartedCount.incrementAndGet();
      lastAggregateIndexName = indexName;
      lastAggregationBuilder = q;
    }

    @Override
    public void aggregateFinished(String indexName, AggregationBuilder q) {
      aggregateFinishedCount.incrementAndGet();
    }

    @Override
    public void dropIndexStarted(String indexName) {
      dropIndexStartedCount.incrementAndGet();
      lastDropIndexName = indexName;
    }

    @Override
    public void dropIndexFinished(String indexName, String result) {
      dropIndexFinishedCount.incrementAndGet();
      lastDropIndexResult = result;
    }

    @Override
    public void infoStarted(String indexName) {
      infoStartedCount.incrementAndGet();
    }

    @Override
    public void infoFinished(String indexName, Map<String, Object> info) {
      infoFinishedCount.incrementAndGet();
    }

    /**
     * Reset all counters and tracked values
     */
    void reset() {
      searchStartedCount.set(0);
      searchFinishedCount.set(0);
      createIndexStartedCount.set(0);
      createIndexFinishedCount.set(0);
      aggregateStartedCount.set(0);
      aggregateFinishedCount.set(0);
      dropIndexStartedCount.set(0);
      dropIndexFinishedCount.set(0);
      infoStartedCount.set(0);
      infoFinishedCount.set(0);
      lastSearchIndexName = null;
      lastSearchQuery = null;
      lastSearchParams = null;
      lastSearchResult = null;
      lastCreateIndexName = null;
      lastCreateParams = null;
      lastCreateFields = null;
      lastCreateSchema = null;
      lastCreateOptions = null;
      lastCreateResult = null;
      lastAggregateIndexName = null;
      lastAggregationBuilder = null;
      lastDropIndexName = null;
      lastDropIndexResult = null;
    }
  }