package com.redis.om.spring.ops.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.Command;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.search.*;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.util.SafeEncoder;

public class SearchOperationsImpl<K> implements SearchOperations<K> {

  RediSearchCommands client;
  K index;
  String indexName;

  public SearchOperationsImpl(K index, RedisModulesClient client) {
    this.index = index;
    this.indexName = index.toString();
    this.client = client.clientForSearch();
  }

  @Override
  public String createIndex(Schema schema, IndexOptions options) {
    return client.ftCreate(indexName, options, schema);
  }

  @Override
  public SearchResult search(Query q) {
    return client.ftSearch(indexName, q);
  }

  @Override
  public AggregationResult aggregate(AggregationBuilder q) {
    return client.ftAggregate(indexName, q);
  }

  @Override
  public String cursorDelete(long cursorId) {
    return client.ftCursorDel(indexName, cursorId);
  }

  @Override
  public AggregationResult cursorRead(long cursorId, int count) {
    return client.ftCursorRead(indexName, cursorId, count);
  }

  @Override
  public String explain(Query q) {
    return client.ftExplain(indexName, q);
  }

  @Override
  public Map<String, Object> getInfo() {
    return client.ftInfo(indexName);
  }

  @Override
  public String dropIndex() {
    return client.ftDropIndex(indexName);
  }

  @Override
  public long addSuggestion(String suggestion, double score) {
    return client.ftSugAdd(indexName, suggestion, score);
  }

  @Override
  public long incrSuggestion(String suggestion, double score) {
    return client.ftSugAddIncr(indexName, suggestion, score);
  }

  @Override
  public List<String> getSuggestion(String prefix) {
    return client.ftSugGet(indexName, prefix);
  }

  @Override
  public List<String> getSuggestion(String prefix, boolean fuzzy, int max) {
    return client.ftSugGet(indexName, prefix, fuzzy, max);
  }

  @Override
  public boolean deleteSuggestion(String entry) {
    return client.ftSugDel(indexName, entry);
  }

  @Override
  public long getSuggestionLength() {
    return client.ftSugLen(indexName);
  }

  @Override
  public String alterIndex(Schema.Field... fields) {
    return client.ftAlter(indexName, fields);
  }

  @Override
  public String setConfig(String option, String value) {
    return client.ftConfigSet(option, value);
  }

  @Override
  public Map<String, String> getConfig(String option) {
    return client.ftConfigGet(option);
  }

  @Override
  public String addAlias(String name) {
    return client.ftAliasAdd(indexName, name);
  }

  @Override
  public String updateAlias(String name) {
    return client.ftAliasUpdate(indexName, name);
  }

  @Override
  public String deleteAlias(String name) {
    return client.ftAliasDel(name);
  }

  @Override
  public String updateSynonym(String synonymGroupId, String... terms) {
    return client.ftSynUpdate(indexName, synonymGroupId, terms);
  }

  @Override
  public Map<String, List<String>> dumpSynonym() {
    return client.ftSynDump(indexName);
  }

  @Override
  public Set<String> tagVals(String field) {
    return client.ftTagVals(indexName, field);
  }

}
