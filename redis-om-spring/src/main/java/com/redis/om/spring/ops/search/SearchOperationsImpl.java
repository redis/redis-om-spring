package com.redis.om.spring.ops.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redis.om.spring.client.RedisModulesClient;

import redis.clients.jedis.search.FTSearchParams;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.RediSearchCommands;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.schemafields.SchemaField;

public class SearchOperationsImpl<K> implements SearchOperations<K> {

  RediSearchCommands client;
  K index;

  public SearchOperationsImpl(K index, RedisModulesClient client) {
    this.index = index;
    this.client = client.clientForSearch(index.toString());
  }

  @Override
  public String createIndex(Schema schema, IndexOptions options) {
    return client.ftCreate(index.toString(), options, schema);
  }

  @Override
  public SearchResult search(Query q) {
    return client.ftSearch(index.toString(), q);
  }
  
  @Override
  public SearchResult search(Query q, FTSearchParams params) {
    return client.ftSearch(index.toString(), q.toString(), params);
  }

  @Override
  public AggregationResult aggregate(AggregationBuilder q) {
    return client.ftAggregate(index.toString(), q);
  }

  @Override
  public String cursorDelete(long cursorId) {
    return client.ftCursorDel(index.toString(), cursorId);
  }

  @Override
  public AggregationResult cursorRead(long cursorId, int count) {
    return client.ftCursorRead(index.toString(), cursorId, count);
  }

  @Override
  public String explain(Query q) {
    return client.ftExplain(index.toString(), q);
  }

  @Override
  public Map<String, Object> getInfo() {
    return client.ftInfo(index.toString());
  }

  @Override
  public String dropIndex() {
    return client.ftDropIndex(index.toString());
  }

  @Override
  public Long addSuggestion(String key, String suggestion) {
     return client.ftSugAdd(key, suggestion, 0);
  }

  @Override
  public List<String> getSuggestion(String key,String prefix) {
    return client.ftSugGet( key, prefix );
  }

  @Override
  public Boolean deleteSuggestion(String key, String entry) {
    return client.ftSugDel(key, entry);
  }
  
  @Override
  public Long getSuggestionLength(String key) {
    return client.ftSugLen(key);
  }

  @Override
  public String alterIndex(SchemaField... fields) {
    return client.ftAlter(index.toString(), fields);
  }

  @Override
  public String setConfig(String option, String value) {
    return client.ftConfigSet(option, value);
  }

  @Override
  public Map<String,String> getConfig(String option) {
    return client.ftConfigGet(option);
  }

  @Override
  public Map<String, String> getIndexConfig(String option) {
    return client.ftConfigGet(index.toString(), option);
  }

  @Override
  public String addAlias(String name) {
    return client.ftAliasAdd(name, index.toString());
  }

  @Override
  public String updateAlias(String name) {
    return client.ftAliasUpdate(name, index.toString());
  }

  @Override
  public String deleteAlias(String name) {
    return client.ftAliasDel(name);
  }

  @Override
  public String updateSynonym(String synonymGroupId, String... terms) {
    return client.ftSynUpdate(index.toString(), synonymGroupId, terms);
  }

  @Override
  public Map<String, List<String>> dumpSynonym() {
    return client.ftSynDump(index.toString());
  }

  @Override
  public Set<String> tagVals(String field) {
    return client.ftTagVals(index.toString(), field);
  }



}
