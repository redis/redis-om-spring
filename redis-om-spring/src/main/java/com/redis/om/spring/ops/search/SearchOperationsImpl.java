package com.redis.om.spring.ops.search;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  private final Optional<CommandListener> commandListener;

  /**
   * Creates a new search operations implementation.
   *
   * @param index         the search index identifier
   * @param modulesClient the Redis modules client for search operations
   * @param template      the string Redis template for additional operations
   * @param commandListener An optional command listener for monitoring Redis commands
   */
  public SearchOperationsImpl(K index, RedisModulesClient modulesClient, StringRedisTemplate template,
                              final Optional<CommandListener> commandListener) {
    this.index = index;
    this.modulesClient = modulesClient;
    this.search = modulesClient.clientForSearch();
    this.template = template;
    this.commandListener = commandListener;
  }

    @Override
    public String createIndex(Schema schema, IndexOptions options) {
      commandListener.ifPresent(it -> it.createIndexStarted(index.toString(), null, null, schema, options));
      final String s = search.ftCreate(index.toString(), options, schema);
      commandListener.ifPresent(it -> it.createIndexFinished(index.toString(), null, null, schema, options, s));
      return s;
    }

    @Override
    public String createIndex(FTCreateParams params, List<SchemaField> fields) {
      commandListener.ifPresent(it -> it.createIndexStarted(index.toString(), params, fields, null, null));
      final String s = search.ftCreate(index.toString(), params, fields);
      commandListener.ifPresent(it -> it.createIndexFinished(index.toString(), params, fields, null, null, s));
      return s;
    }

    @Override
    @Deprecated
    public SearchResult search(Query q) {
      commandListener.ifPresent(it -> it.searchStarted(index.toString(), q, null));
      final SearchResult searchResult = search.ftSearch(SafeEncoder.encode(index.toString()), q);
      commandListener.ifPresent(it -> it.searchFinished(index.toString(), q, null, searchResult));
      return searchResult;
    }

    @Override
    public SearchResult search(Query q, FTSearchParams params) {
      commandListener.ifPresent(it -> it.searchStarted(index.toString(), q, null));
      final SearchResult searchResult = search.ftSearch(index.toString(), q.toString(), params);
      commandListener.ifPresent(it -> it.searchFinished(index.toString(), q, null, searchResult));
      return searchResult;
    }

    @Override
    public AggregationResult aggregate(AggregationBuilder q) {
      commandListener.ifPresent(it -> it.aggregateStarted(index.toString(), q));
      final AggregationResult aggregationResult = search.ftAggregate(index.toString(), q);
      commandListener.ifPresent(it -> it.aggregateFinished(index.toString(), q));
      return aggregationResult;
    }

    @Override
    public String cursorDelete(long cursorId) {
      commandListener.ifPresent(it -> it.cursorDeleteStarted(index.toString(), cursorId));
      final String result = search.ftCursorDel(index.toString(), cursorId);
      commandListener.ifPresent(it -> it.cursorDeleteFinished(index.toString(), cursorId, result));
      return result;
    }

  @Override
  public AggregationResult cursorRead(long cursorId, int count) {
    commandListener.ifPresent(it -> it.cursorReadStarted(index.toString(), cursorId, count));
    final AggregationResult aggregationResult = search.ftCursorRead(index.toString(), cursorId, count);
    commandListener.ifPresent(it -> it.cursorReadFinished(index.toString(), cursorId, count, aggregationResult));
    return aggregationResult;
  }

  @Override
  public String explain(Query q) {
    commandListener.ifPresent(it -> it.explainStarted(index.toString(), q));
    final String s = search.ftExplain(index.toString(), q);
    commandListener.ifPresent(it -> it.explainFinished(index.toString(), q, s));
    return s;
  }

  @Override
  public Map<String, Object> getInfo() {
    commandListener.ifPresent(it -> it.infoStarted(index.toString()));
    final Map<String, Object> result = search.ftInfo(index.toString());
    commandListener.ifPresent(it -> it.infoFinished(index.toString(), result));
    return result;
  }

  @Override
  public String dropIndex() {
    commandListener.ifPresent(it -> it.dropIndexStarted(index.toString()));
    final String result = search.ftDropIndex(index.toString());
    commandListener.ifPresent(it -> it.dropIndexFinished(index.toString(), result));
    return result;
  }

  @Override
  public String dropIndexAndDocuments() {
    commandListener.ifPresent(it -> it.dropIndexAndDocumentsStarted(index.toString()));
    final String result = search.ftDropIndexDD(index.toString());
    commandListener.ifPresent(it -> it.dropIndexAndDocumentsFinished(index.toString(), result));
    return result;
  }

  @Override
  public Long addSuggestion(String key, String suggestion) {
    return addSuggestion(key, suggestion, 1.0);
  }

  @Override
  public Long addSuggestion(String key, String suggestion, double score) {
    commandListener.ifPresent(it -> it.addSuggestionStarted(index.toString(), key, suggestion, score));
    final long result = search.ftSugAdd(key, suggestion, score);
    commandListener.ifPresent(it -> it.addSuggestionFinished(index.toString(), key, suggestion, score, result));
    return result;
  }

  @Override
  public List<Suggestion> getSuggestion(String key, String prefix) {
    return this.getSuggestion(key, prefix, AutoCompleteOptions.get());
  }

  @Override
  public List<Suggestion> getSuggestion(String key, String prefix, AutoCompleteOptions options) {
    commandListener.ifPresent(it -> it.getSuggestionStarted(index.toString(), key, prefix, options));
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
      commandListener.ifPresent(it -> it.getSuggestionFinished(index.toString(), key, prefix, options, list));
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
        commandListener.ifPresent(it -> it.getSuggestionFinished(index.toString(), key, prefix, options, list));
        return list;
    }
  }

  @Override
  public Boolean deleteSuggestion(String key, String entry) {
    commandListener.ifPresent(it -> it.deleteSuggestionStarted(index.toString(), key, entry));
    final boolean result = search.ftSugDel(key, entry);
    commandListener.ifPresent(it -> it.deleteSuggestionFinished(index.toString(), key, entry, result));
    return result;
  }

  @Override
  public Long getSuggestionLength(String key) {
    commandListener.ifPresent(it -> it.getSuggestionLengthStarted(index.toString(), key));
    final long result = search.ftSugLen(key);
    commandListener.ifPresent(it -> it.getSuggestionLengthFinished(index.toString(), key, result));
    return result;
  }

  @Override
  public String alterIndex(SchemaField... fields) {
    commandListener.ifPresent(it -> it.alterIndexStarted(index.toString(), fields));
    final String result = search.ftAlter(index.toString(), fields);
    commandListener.ifPresent(it -> it.alterIndexFinished(index.toString(), fields, result));
    return result;
  }

  @Override
  public String setConfig(String option, String value) {
    commandListener.ifPresent(it -> it.setConfigStarted(index.toString(), option, value));
    final String result = search.ftConfigSet(option, value);
    commandListener.ifPresent(it -> it.setConfigFinished(index.toString(), option, value, result));
    return result;
  }

  @Override
  public Map<String, Object> getConfig(String option) {
    commandListener.ifPresent(it -> it.getConfigStarted(index.toString(), option));
    final Map<String, Object> result = search.ftConfigGet(option);
    commandListener.ifPresent(it -> it.getConfigFinished(index.toString(), option, result));
    return result;
  }

  @Override
  public Map<String, Object> getIndexConfig(String option) {
    commandListener.ifPresent(it -> it.getIndexConfigStarted(index.toString(), option));
    final Map<String, Object> result = search.ftConfigGet(index.toString(), option);
    commandListener.ifPresent(it -> it.getIndexConfigFinished(index.toString(), option, result));
    return result;
  }

  @Override
  public String addAlias(String name) {
    commandListener.ifPresent(it -> it.addAliasStarted(index.toString(), name));
    final String result = search.ftAliasAdd(name, index.toString());
    commandListener.ifPresent(it -> it.addAliasFinished(index.toString(), name, result));
    return result;
  }

  @Override
  public String updateAlias(String name) {
    commandListener.ifPresent(it -> it.updateAliasStarted(index.toString(), name));
    final String result = search.ftAliasUpdate(name, index.toString());
    commandListener.ifPresent(it -> it.updateAliasFinished(index.toString(), name, result));
    return result;
  }

  @Override
  public String deleteAlias(String name) {
    commandListener.ifPresent(it -> it.deleteAliasStarted(index.toString(), name));
    final String result = search.ftAliasDel(name);
    commandListener.ifPresent(it -> it.deleteAliasFinished(index.toString(), name, result));
    return result;
  }

  @Override
  public String updateSynonym(String synonymGroupId, String... terms) {
    commandListener.ifPresent(it -> it.updateSynonymStarted(index.toString(), synonymGroupId, terms));
    final String result = search.ftSynUpdate(index.toString(), synonymGroupId, terms);
    commandListener.ifPresent(it -> it.updateSynonymFinished(index.toString(), synonymGroupId, terms, result));
    return result;
  }

  @Override
  public Map<String, List<String>> dumpSynonym() {
    commandListener.ifPresent(it -> it.dumpSynonymStarted(index.toString()));
    final Map<String, List<String>> result = search.ftSynDump(index.toString());
    commandListener.ifPresent(it -> it.dumpSynonymFinished(index.toString(), result));
    return result;
  }

  @Override
  public Set<String> tagVals(String field) {
    commandListener.ifPresent(it -> it.tagValsStarted(index.toString(), field));
    final Set<String> result = search.ftTagVals(index.toString(), field);
    commandListener.ifPresent(it -> it.tagValsFinished(index.toString(), field, result));
    return result;
  }

}
