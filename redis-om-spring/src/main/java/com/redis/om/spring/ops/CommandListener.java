package com.redis.om.spring.ops;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redis.om.spring.autocomplete.Suggestion;
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteOptions;

import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.FTSearchParams;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.SearchProtocol;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.schemafields.SchemaField;

public interface CommandListener {
  default void searchStarted(String indexName, Query q, FTSearchParams params) {
  }

  default void searchFinished(String indexName, Query q, FTSearchParams params, SearchResult searchResult) {
  }

  default void createIndexStarted(String indexName, FTCreateParams params, List<SchemaField> fields, Schema schema,
      IndexOptions options) {
  }

  default void createIndexFinished(String indexName, FTCreateParams params, List<SchemaField> fields, Schema schema,
      IndexOptions options, String result) {
  }

  default void aggregateStarted(String indexName, AggregationBuilder q) {
  }

  default void aggregateFinished(String indexName, AggregationBuilder q, AggregationResult result) {
  }

  default void cursorDeleteStarted(String string, long cursorId) {
  }

  default void cursorDeleteFinished(String string, long cursorId, String result) {
  }

  default void cursorReadStarted(String string, long cursorId, int count) {
  }

  default void cursorReadFinished(String string, long cursorId, int count, AggregationResult aggregationResult) {
  }

  default void explainStarted(String string, Query q) {
  }

  default void explainFinished(String string, Query q, String s) {
  }

  default void infoStarted(String string) {
  }

  default void infoFinished(String string, Map<String, Object> stringObjectMap) {
  }

  default void dropIndexStarted(String string) {
  }

  default void dropIndexFinished(String string, String result) {
  }

  default void dropIndexAndDocumentsStarted(String string) {
  }

  default void dropIndexAndDocumentsFinished(String string, String result) {
  }

  default void addSuggestionStarted(String string, String key, String suggestion, double score) {
  }

  default void addSuggestionFinished(String string, String key, String suggestion, double score, long result) {
  }

  default void getSuggestionStarted(String string, String key, String prefix, AutoCompleteOptions options) {
  }

  default void getSuggestionFinished(String string, String key, String prefix, AutoCompleteOptions options,
      List<Suggestion> list) {
  }

  default void deleteSuggestionStarted(String string, String key, String entry) {
  }

  default void deleteSuggestionFinished(String string, String key, String entry, boolean result) {
  }

  default void getSuggestionLengthStarted(String string, String key) {
  }

  default void getSuggestionLengthFinished(String string, String key, long result) {
  }

  default void alterIndexStarted(String string, SchemaField[] fields) {
  }

  default void alterIndexFinished(String string, SchemaField[] fields, String result) {
  }

  default void setConfigStarted(String string, String option, String value) {
  }

  default void setConfigFinished(String string, String option, String value, String result) {
  }

  default void getConfigStarted(String string, String option) {
  }

  default void getConfigFinished(String string, String option, Map<String, Object> result) {
  }

  default void getIndexConfigStarted(String string, String option) {
  }

  default void getIndexConfigFinished(String string, String option, Map<String, Object> result) {
  }

  default void addAliasStarted(String string, String name) {
  }

  default void addAliasFinished(String string, String name, String result) {
  }

  default void updateAliasStarted(String string, String name) {
  }

  default void updateAliasFinished(String string, String name, String result) {
  }

  default void deleteAliasStarted(String string, String name) {
  }

  default void deleteAliasFinished(String string, String name, String result) {
  }

  default void updateSynonymStarted(String string, String synonymGroupId, String[] terms) {
  }

  default void updateSynonymFinished(String string, String synonymGroupId, String[] terms, String result) {
  }

  default void dumpSynonymStarted(String string) {
  }

  default void dumpSynonymFinished(String string, Map<String, List<String>> result) {
  }

  default void tagValsStarted(String string, String field) {
  }

  default void tagValsFinished(String string, String field, Set<String> result) {
  }

  default void commandFailed(SearchProtocol.SearchCommand command, String indexName, Throwable t) {
  }
}
