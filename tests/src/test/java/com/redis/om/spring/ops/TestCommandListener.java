package com.redis.om.spring.ops;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.FTSearchParams;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
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
    AtomicInteger dropIndexAndDocumentsStartedCount = new AtomicInteger(0);
    AtomicInteger dropIndexAndDocumentsFinishedCount = new AtomicInteger(0);
    String lastDropIndexName;
    String lastDropIndexResult;

    // Info tracking
    AtomicInteger infoStartedCount = new AtomicInteger(0);
    AtomicInteger infoFinishedCount = new AtomicInteger(0);

    // Alter index tracking
    AtomicInteger alterIndexStartedCount = new AtomicInteger(0);
    AtomicInteger alterIndexFinishedCount = new AtomicInteger(0);

    // Set config tracking
    AtomicInteger setConfigStartedCount = new AtomicInteger(0);
    AtomicInteger setConfigFinishedCount = new AtomicInteger(0);

    // Get config tracking
    AtomicInteger getConfigStartedCount = new AtomicInteger(0);
    AtomicInteger getConfigFinishedCount = new AtomicInteger(0);

    // Get index config tracking
    AtomicInteger getIndexConfigStartedCount = new AtomicInteger(0);
    AtomicInteger getIndexConfigFinishedCount = new AtomicInteger(0);

    // Add alias tracking
    AtomicInteger addAliasStartedCount = new AtomicInteger(0);
    AtomicInteger addAliasFinishedCount = new AtomicInteger(0);

    // Update alias tracking
    AtomicInteger updateAliasStartedCount = new AtomicInteger(0);
    AtomicInteger updateAliasFinishedCount = new AtomicInteger(0);

    // Delete alias tracking
    AtomicInteger deleteAliasStartedCount = new AtomicInteger(0);
    AtomicInteger deleteAliasFinishedCount = new AtomicInteger(0);

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
    public void aggregateFinished(final String indexName, final AggregationBuilder q, final AggregationResult aggregationResult) {
      aggregateFinishedCount.incrementAndGet();
    }

    @Override
    public void infoStarted(String indexName) {
        infoStartedCount.incrementAndGet();
    }

    @Override
    public void infoFinished(String indexName, Map<String, Object> info) {
        infoFinishedCount.incrementAndGet();
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
    public void dropIndexAndDocumentsStarted(String string) {
        dropIndexAndDocumentsStartedCount.incrementAndGet();
    }

    @Override
    public void dropIndexAndDocumentsFinished(String string, String result) {
        dropIndexAndDocumentsFinishedCount.incrementAndGet();
        lastDropIndexResult = result;
    }

    @Override
    public void alterIndexStarted(String string, SchemaField[] fields) {
        alterIndexStartedCount.incrementAndGet();
    }

    @Override
    public void alterIndexFinished(String string, SchemaField[] fields, String result) {
        alterIndexFinishedCount.incrementAndGet();
    }

    @Override
    public void setConfigStarted(String string, String option, String value) {
        setConfigStartedCount.incrementAndGet();
    }

    @Override
    public void setConfigFinished(String string, String option, String value, String result) {
        setConfigFinishedCount.incrementAndGet();
    }

    @Override
    public void getConfigStarted(String string, String option) {
        getConfigStartedCount.incrementAndGet();
    }

    @Override
    public void getConfigFinished(String string, String option, Map<String, Object> result) {
        getConfigFinishedCount.incrementAndGet();
    }

    @Override
    public void getIndexConfigStarted(String string, String option) {
        getIndexConfigStartedCount.incrementAndGet();
    }

    @Override
    public void getIndexConfigFinished(String string, String option, Map<String, Object> result) {
        getIndexConfigFinishedCount.incrementAndGet();
    }

    @Override
    public void addAliasStarted(String string, String name) {
        addAliasStartedCount.incrementAndGet();
    }

    @Override
    public void addAliasFinished(String string, String name, String result) {
        addAliasFinishedCount.incrementAndGet();
        lastDropIndexResult = result;
    }

    @Override
    public void updateAliasStarted(String string, String name) {
        updateAliasStartedCount.incrementAndGet();
    }

    @Override
    public void updateAliasFinished(String string, String name, String result) {
        updateAliasFinishedCount.incrementAndGet();
        lastDropIndexResult = result;
    }

    @Override
    public void deleteAliasStarted(String string, String name) {
        deleteAliasStartedCount.incrementAndGet();
    }

    @Override
    public void deleteAliasFinished(String string, String name, String result) {
        deleteAliasFinishedCount.incrementAndGet();
        lastDropIndexResult = result;
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
        alterIndexStartedCount.set(0);
        alterIndexFinishedCount.set(0);
        setConfigStartedCount.set(0);
        setConfigFinishedCount.set(0);
        getConfigStartedCount.set(0);
        getConfigFinishedCount.set(0);
        getIndexConfigStartedCount.set(0);
        getIndexConfigFinishedCount.set(0);
        alterIndexStartedCount.set(0);
        alterIndexStartedCount.set(0);
        addAliasStartedCount.set(0);
        addAliasFinishedCount.set(0);
        updateAliasStartedCount.set(0);
        updateAliasFinishedCount.set(0);
        deleteAliasStartedCount.set(0);
        deleteAliasFinishedCount.set(0);
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