package com.redis.om.spring.ops.search;

import com.redis.om.spring.autocomplete.Suggestion;
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteOptions;
import redis.clients.jedis.search.*;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.schemafields.SchemaField;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SearchOperations<K> {

  String createIndex(Schema schema, IndexOptions options);
  SearchResult search(Query q);
  SearchResult search(Query q, FTSearchParams params);
  AggregationResult aggregate(AggregationBuilder q);   
  String cursorDelete(long cursorId);
  AggregationResult cursorRead(long cursorId, int count);
  String explain(Query q);
  Map<String, Object> getInfo();
  String dropIndex();
  String dropIndexAndDocuments();
  Long addSuggestion(String key, String suggestion);
  Long addSuggestion(String key, String suggestion, double score);
  List<Suggestion> getSuggestion(String key, String prefix);
  List<Suggestion> getSuggestion(String key, String prefix, AutoCompleteOptions options);
  Boolean deleteSuggestion(String key, String entry);
  Long getSuggestionLength(String key);
  String alterIndex(SchemaField... fields);
  String setConfig(String option, String value);
  Map<String, String> getConfig(String option);
  Map<String, String> getIndexConfig(String option);
  String addAlias(String name);
  String updateAlias(String name);
  String deleteAlias(String name);
  String updateSynonym(String synonymGroupId, String ...terms);
  Map<String, List<String>> dumpSynonym();
  Set<String> tagVals(String value);
  
}
