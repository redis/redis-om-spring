package com.redis.om.spring.ops.search;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.redis.om.spring.ops.CommandListener;
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
  private final CommandListener commandListener;

  /**
   * Creates a new search operations implementation.
   *
   * @param index         the search index identifier
   * @param modulesClient the Redis modules client for search operations
   * @param template      the string Redis template for additional operations
   * @param commandListener A command listener for monitoring Redis commands
   */
  public SearchOperationsImpl(K index, RedisModulesClient modulesClient, StringRedisTemplate template,
                              final CommandListener commandListener) {
    this.index = index;
    this.modulesClient = modulesClient;
    this.search = modulesClient.clientForSearch();
    this.template = template;
    this.commandListener = commandListener;
  }

    @Override
    public String createIndex(Schema schema, IndexOptions options) {
      commandListener.createIndexStarted(index.toString(), null, null, schema, options);
      final String s = search.ftCreate(index.toString(), options, schema);
      commandListener.createIndexFinished(index.toString(), null, null, schema, options, s);
      return s;
    }

    @Override
    public String createIndex(FTCreateParams params, List<SchemaField> fields) {
      commandListener.createIndexStarted(index.toString(), params, fields, null, null);
      final String s = search.ftCreate(index.toString(), params, fields);
      commandListener.createIndexFinished(index.toString(), params, fields, null, null, s);
      return s;
    }

    @Override
    @Deprecated
    public SearchResult search(Query q) {
      commandListener.searchStarted(index.toString(), q, null);
      final SearchResult searchResult = search.ftSearch(SafeEncoder.encode(index.toString()), q);
      commandListener.searchFinished(index.toString(), q, null, searchResult);
      return searchResult;
    }

    @Override
    public SearchResult search(Query q, FTSearchParams params) {
      commandListener.searchStarted(index.toString(), q, null);
      final SearchResult searchResult = search.ftSearch(index.toString(), q.toString(), params);
      commandListener.searchFinished(index.toString(), q, null, searchResult);
      return searchResult;
    }

    @Override
    public AggregationResult aggregate(AggregationBuilder q) {
      commandListener.aggregateStarted(index.toString(), q);
      final AggregationResult aggregationResult = search.ftAggregate(index.toString(), q);
      commandListener.aggregateFinished(index.toString(), q);
      return aggregationResult;
    }

    @Override
    public String cursorDelete(long cursorId) {
      commandListener.cursorDeleteStarted(index.toString(), cursorId);
      final String result = search.ftCursorDel(index.toString(), cursorId);
      commandListener.cursorDeleteFinished(index.toString(), cursorId, result);
      return result;
    }

  @Override
  public AggregationResult cursorRead(long cursorId, int count) {
    commandListener.cursorReadStarted(index.toString(), cursorId, count);
    final AggregationResult aggregationResult = search.ftCursorRead(index.toString(), cursorId, count);
    commandListener.cursorReadFinished(index.toString(), cursorId, count, aggregationResult);
    return aggregationResult;
  }

  @Override
  public String explain(Query q) {
    commandListener.explainStarted(index.toString(), q);
    final String s = search.ftExplain(index.toString(), q);
    commandListener.explainFinished(index.toString(), q, s);
    return s;
  }

  @Override
  public Map<String, Object> getInfo() {
    commandListener.infoStarted(index.toString());
    final Map<String, Object> result = search.ftInfo(index.toString());
    commandListener.infoFinished(index.toString(), result);
    return result;
  }

  @Override
  public String dropIndex() {
    commandListener.dropIndexStarted(index.toString());
    final String result = search.ftDropIndex(index.toString());
    commandListener.dropIndexFinished(index.toString(), result);
    return result;
  }

  @Override
  public String dropIndexAndDocuments() {
    commandListener.dropIndexAndDocumentsStarted(index.toString());
    final String result = search.ftDropIndexDD(index.toString());
    commandListener.dropIndexAndDocumentsFinished(index.toString(), result);
    return result;
  }

  @Override
  public Long addSuggestion(String key, String suggestion) {
    return addSuggestion(key, suggestion, 1.0);
  }

  @Override
  public Long addSuggestion(String key, String suggestion, double score) {
    commandListener.addSuggestionStarted(index.toString(), key, suggestion, score);
    final long result = search.ftSugAdd(key, suggestion, score);
    commandListener.addSuggestionFinished(index.toString(), key, suggestion, score, result);
    return result;
  }

  @Override
  public List<Suggestion> getSuggestion(String key, String prefix) {
    return this.getSuggestion(key, prefix, AutoCompleteOptions.get());
  }

  @Override
  public List<Suggestion> getSuggestion(String key, String prefix, AutoCompleteOptions options) {
    commandListener.getSuggestionStarted(index.toString(), key, prefix, options);
    Gson gson = modulesClient.gsonBuilder().create();

    if (options.isWithScore()) {
      List<Tuple> suggestions = search.ftSugGetWithScores(key, prefix, options.isFuzzy(), options.getLimit());
      List<Suggestion> list = suggestions.stream().map(suggestion -> {
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
        commandListener.getSuggestionFinished(index.toString(), key, prefix, options, list);
      return list;
    } else {
      List<String> suggestions = search.ftSugGet(key, prefix, options.isFuzzy(), options.getLimit());
        List<Suggestion> list = suggestions.stream().map(suggestion -> {
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
        commandListener.getSuggestionFinished(index.toString(), key, prefix, options, list);
        return list;
    }
  }

  @Override
  public Boolean deleteSuggestion(String key, String entry) {
    commandListener.deleteSuggestionStarted(index.toString(), key, entry);
    final boolean result = search.ftSugDel(key, entry);
    commandListener.deleteSuggestionFinished(index.toString(), key, entry, result);
    return result;
  }

  @Override
  public Long getSuggestionLength(String key) {
    commandListener.getSuggestionLengthStarted(index.toString(), key);
    final long result = search.ftSugLen(key);
    commandListener.getSuggestionLengthFinished(index.toString(), key, result);
    return result;
  }

  @Override
  public String alterIndex(SchemaField... fields) {
    commandListener.alterIndexStarted(index.toString(), fields);
    final String result = search.ftAlter(index.toString(), fields);
    commandListener.alterIndexFinished(index.toString(), fields, result);
    return result;
  }

  @Override
  public String setConfig(String option, String value) {
    commandListener.setConfigStarted(index.toString(), option, value);
    final String result = search.ftConfigSet(option, value);
    commandListener.setConfigFinished(index.toString(), option, value, result);
    return result;
  }

  @Override
  public Map<String, Object> getConfig(String option) {
    commandListener.getConfigStarted(index.toString(), option);
    final Map<String, Object> result = search.ftConfigGet(option);
    commandListener.getConfigFinished(index.toString(), option, result);
    return result;
  }

  @Override
  public Map<String, Object> getIndexConfig(String option) {
    commandListener.getIndexConfigStarted(index.toString(), option);
    final Map<String, Object> result = search.ftConfigGet(index.toString(), option);
    commandListener.getIndexConfigFinished(index.toString(), option, result);
    return result;
  }

  @Override
  public String addAlias(String name) {
    commandListener.addAliasStarted(index.toString(), name);
    final String result = search.ftAliasAdd(name, index.toString());
    commandListener.addAliasFinished(index.toString(), name, result);
    return result;
  }

  @Override
  public String updateAlias(String name) {
    commandListener.updateAliasStarted(index.toString(), name);
    final String result = search.ftAliasUpdate(name, index.toString());
    commandListener.updateAliasFinished(index.toString(), name, result);
    return result;
  }

  @Override
  public String deleteAlias(String name) {
    commandListener.deleteAliasStarted(index.toString(), name);
    final String result = search.ftAliasDel(name);
    commandListener.deleteAliasFinished(index.toString(), name, result);
    return result;
  }

  @Override
  public String updateSynonym(String synonymGroupId, String... terms) {
    commandListener.updateSynonymStarted(index.toString(), synonymGroupId, terms);
    final String result = search.ftSynUpdate(index.toString(), synonymGroupId, terms);
    commandListener.updateSynonymFinished(index.toString(), synonymGroupId, terms, result);
    return result;
  }

  @Override
  public Map<String, List<String>> dumpSynonym() {
    commandListener.dumpSynonymStarted(index.toString());
    final Map<String, List<String>> result = search.ftSynDump(index.toString());
    commandListener.dumpSynonymFinished(index.toString(), result);
    return result;
  }

  @Override
  public Set<String> tagVals(String field) {
    commandListener.tagValsStarted(index.toString(), field);
    final Set<String> result = search.ftTagVals(index.toString(), field);
    commandListener.tagValsFinished(index.toString(), field, result);
    return result;
  }

}
