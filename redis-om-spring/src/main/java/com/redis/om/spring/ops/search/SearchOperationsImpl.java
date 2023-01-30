package com.redis.om.spring.ops.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.autocomplete.Suggestion;
import com.redis.om.spring.client.RedisModulesClient;

import com.redis.om.spring.repository.query.autocomplete.AutoCompleteOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.resps.Tuple;
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

  private final RediSearchCommands search;
  private final RedisModulesClient modulesClient;
  private final K index;
  private final StringRedisTemplate template;

  public SearchOperationsImpl(K index, RedisModulesClient modulesClient, StringRedisTemplate template) {
    this.index = index;
    this.modulesClient = modulesClient;
    this.search = modulesClient.clientForSearch();
    this.template = template;
  }

  @Override
  public String createIndex(Schema schema, IndexOptions options) {
    return search.ftCreate(index.toString(), options, schema);
  }

  @Override
  public SearchResult search(Query q) {
    return search.ftSearch(index.toString(), q);
  }
  
  @Override
  public SearchResult search(Query q, FTSearchParams params) {
    return search.ftSearch(index.toString(), q.toString(), params);
  }

  @Override
  public AggregationResult aggregate(AggregationBuilder q) {
    return search.ftAggregate(index.toString(), q);
  }

  @Override
  public String cursorDelete(long cursorId) {
    return search.ftCursorDel(index.toString(), cursorId);
  }

  @Override
  public AggregationResult cursorRead(long cursorId, int count) {
    return search.ftCursorRead(index.toString(), cursorId, count);
  }

  @Override
  public String explain(Query q) {
    return search.ftExplain(index.toString(), q);
  }

  @Override
  public Map<String, Object> getInfo() {
    return search.ftInfo(index.toString());
  }

  @Override
  public String dropIndex() {
    return search.ftDropIndex(index.toString());
  }

  @Override
  public String dropIndexAndDocuments() {
    return search.ftDropIndexDD(index.toString());
  }

  @Override
  public Long addSuggestion(String key, String suggestion) {
     return search.ftSugAdd(key, suggestion, 1.0);
  }

  @Override
  public Long addSuggestion(String key, String suggestion, double score) {
    return search.ftSugAdd(key, suggestion, score);
  }

  @Override
  public List<Suggestion> getSuggestion(String key,String prefix) {
    return this.getSuggestion(key, prefix, AutoCompleteOptions.get());
  }

  @Override public List<Suggestion> getSuggestion(String key, String prefix, AutoCompleteOptions options) {
    Gson gson = modulesClient.gsonBuilder().create();

    if (options.isWithScore()) {
      List<Tuple> suggestions = search.ftSugGetWithScores(key, prefix, options.isFuzzy(), options.getLimit());
      return suggestions.stream().map(suggestion -> {
        if (options.isWithPayload()) {
          String[] keyParts = key.split(":");
          String payLoadKey = String.format("sugg:payload:%s:%s", keyParts[keyParts.length - 2], keyParts[keyParts.length - 1]);
          Object payload = template.opsForHash().get(payLoadKey, suggestion);
          String json = payload != null ? payload.toString() : "{}";
          Map<String, Object> payloadMap = gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
          return new Suggestion(suggestion.getElement(), suggestion.getScore(), payloadMap);
        } else {
          return new Suggestion(suggestion.getElement(), suggestion.getScore());
        }
      }).toList();
    } else {
      List<String> suggestions = search.ftSugGet(key, prefix, options.isFuzzy(), options.getLimit());
      return suggestions.stream().map(suggestion -> {
        if (options.isWithPayload()) {
          String[] keyParts = key.split(":");
          String payLoadKey = String.format("sugg:payload:%s:%s", keyParts[keyParts.length - 2], keyParts[keyParts.length - 1]);
          Object payload = template.opsForHash().get(payLoadKey, suggestion);
          String json = payload != null ? payload.toString() : "{}";
          Map<String, Object> payloadMap = gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
          return new Suggestion(suggestion, payloadMap);
        } else {
          return new Suggestion(suggestion);
        }
      }).toList();
    }
  }

  @Override
  public Boolean deleteSuggestion(String key, String entry) {
    return search.ftSugDel(key, entry);
  }
  
  @Override
  public Long getSuggestionLength(String key) {
    return search.ftSugLen(key);
  }

  @Override
  public String alterIndex(SchemaField... fields) {
    return search.ftAlter(index.toString(), fields);
  }

  @Override
  public String setConfig(String option, String value) {
    return search.ftConfigSet(option, value);
  }

  @Override
  public Map<String,String> getConfig(String option) {
    return search.ftConfigGet(option);
  }

  @Override
  public Map<String, String> getIndexConfig(String option) {
    return search.ftConfigGet(index.toString(), option);
  }

  @Override
  public String addAlias(String name) {
    return search.ftAliasAdd(name, index.toString());
  }

  @Override
  public String updateAlias(String name) {
    return search.ftAliasUpdate(name, index.toString());
  }

  @Override
  public String deleteAlias(String name) {
    return search.ftAliasDel(name);
  }

  @Override
  public String updateSynonym(String synonymGroupId, String... terms) {
    return search.ftSynUpdate(index.toString(), synonymGroupId, terms);
  }

  @Override
  public Map<String, List<String>> dumpSynonym() {
    return search.ftSynDump(index.toString());
  }

  @Override
  public Set<String> tagVals(String field) {
    return search.ftTagVals(index.toString(), field);
  }



}
