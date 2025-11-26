package com.redis.om.spring.ops.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.autocomplete.Suggestion;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteOptions;

import redis.clients.jedis.resps.Tuple;
import redis.clients.jedis.search.*;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.schemafields.SchemaField;
import redis.clients.jedis.util.SafeEncoder;

/**
 * Implementation of search operations using Redis Search (RediSearch) module.
 * <p>
 * This class provides a concrete implementation of {@link SearchOperations} that wraps
 * the Redis Search functionality, including index management, document searching,
 * aggregations, and auto-completion features.
 * </p>
 * <p>
 * The implementation uses Jedis clients to communicate with Redis and provides
 * type-safe operations for working with search indexes.
 * </p>
 *
 * @param <K> the type of the search index key
 */
public class SearchOperationsImpl<K> implements SearchOperations<K> {

  private final RediSearchCommands search;
  private final RedisModulesClient modulesClient;
  private final K index;
  private final StringRedisTemplate template;

  /**
   * Creates a new search operations implementation.
   *
   * @param index         the search index identifier
   * @param modulesClient the Redis modules client for search operations
   * @param template      the string Redis template for additional operations
   */
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
  public String createIndex(FTCreateParams params, List<SchemaField> fields) {
    return search.ftCreate(index.toString(), params, fields);
  }

  @Override
  public SearchResult search(Query q) {
    return search.ftSearch(SafeEncoder.encode(index.toString()), q);
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
  public List<Suggestion> getSuggestion(String key, String prefix) {
    return this.getSuggestion(key, prefix, AutoCompleteOptions.get());
  }

  @Override
  public List<Suggestion> getSuggestion(String key, String prefix, AutoCompleteOptions options) {
    Gson gson = modulesClient.gsonBuilder().create();

    if (options.isWithScore()) {
      List<Tuple> suggestions = search.ftSugGetWithScores(key, prefix, options.isFuzzy(), options.getLimit());
      return suggestions.stream().map(suggestion -> {
        if (options.isWithPayload()) {
          String[] keyParts = key.split(":");
          String payLoadKey = String.format("sugg:payload:%s:%s", keyParts[keyParts.length - 2],
              keyParts[keyParts.length - 1]);
          Object payload = template.opsForHash().get(payLoadKey, suggestion.getElement());
          String json = payload != null ? payload.toString() : "{}";
          Map<String, Object> payloadMap = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
          }.getType());
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
          String payLoadKey = String.format("sugg:payload:%s:%s", keyParts[keyParts.length - 2],
              keyParts[keyParts.length - 1]);
          Object payload = template.opsForHash().get(payLoadKey, suggestion);
          String json = payload != null ? payload.toString() : "{}";
          Map<String, Object> payloadMap = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
          }.getType());
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
  public Map<String, Object> getConfig(String option) {
    return search.ftConfigGet(option);
  }

  @Override
  public Map<String, Object> getIndexConfig(String option) {
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
