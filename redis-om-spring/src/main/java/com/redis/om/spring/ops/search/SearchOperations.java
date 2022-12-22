package com.redis.om.spring.ops.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.schemafields.SchemaField;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.FTSearchParams;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.Schema.Field;
import redis.clients.jedis.search.SearchResult;
//import redis.clients.jedis.search.Suggestion;
import redis.clients.jedis.search.aggr.AggregationBuilder;
//import redis.clients.jedis.search.AddOptions;
//import redis.clients.jedis.search.ConfigOption;
//import redis.clients.jedis.search.SuggestionOptions;
import redis.clients.jedis.search.IndexOptions;

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
  Long addSuggestion(String key, String suggestion);
  List<String> getSuggestion(String key, String prefix);
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
