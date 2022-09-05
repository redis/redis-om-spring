package com.redis.om.spring.ops.search;

import java.util.List;
import java.util.Map;

import redis.clients.jedis.search.*;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;

public interface SearchOperations<K> {

  String createIndex(Schema schema, IndexOptions options);
  SearchResult search(Query q);
  AggregationResult aggregate(AggregationBuilder q);
  String cursorDelete(long cursorId);
  AggregationResult cursorRead(long cursorId, int count);
  String explain(Query q);
  Map<String, Object> getInfo();
  String dropIndex();
  long addSuggestion(String string, double score);
  long incrSuggestion(String string, double score);
  List<String> getSuggestion(String prefix);
  List<String> getSuggestion(String prefix, boolean fuzzy, int max);
  boolean deleteSuggestion(String entry);
  long getSuggestionLength();
  String alterIndex(Schema.Field...fields);
  String setConfig(String option, String value);
  Map<String, String> getConfig(String option);
  default Map<String, String> getAllConfig() {return getConfig("*");}
  boolean addAlias(String name);
  boolean updateAlias(String name);
  boolean deleteAlias(String name);
  boolean updateSynonym(String synonymGroupId, String ...terms);
  Map<String, List<String>> dumpSynonym();
  List<String> tagVals(String value);
}
