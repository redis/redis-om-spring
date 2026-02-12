package com.redis.om.spring.ops.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.autocomplete.Suggestion;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.CommandListener;
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
   * @param index           the search index identifier
   * @param modulesClient   the Redis modules client for search operations
   * @param template        the string Redis template for additional operations
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
    String result = null;
    try {
      result = search.ftCreate(index.toString(), options, schema);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.createIndexFinished(index.toString(), null, null, schema, options, result);
    }
    return result;
  }

  @Override
  public String createIndex(FTCreateParams params, List<SchemaField> fields) {
    commandListener.createIndexStarted(index.toString(), params, fields, null, null);
    String result = null;
    try {
      result = search.ftCreate(index.toString(), params, fields);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.createIndexFinished(index.toString(), params, fields, null, null, result);
    }
    return result;
  }

  @Override
  @Deprecated
  public SearchResult search(Query q) {
    commandListener.searchStarted(index.toString(), q, null);
    SearchResult result = null;
    try {
      result = search.ftSearch(SafeEncoder.encode(index.toString()), q);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.searchFinished(index.toString(), q, null, result);
    }
    return result;
  }

  @Override
  public SearchResult search(Query q, FTSearchParams params) {
    commandListener.searchStarted(index.toString(), q, null);
    final SearchResult result = search.ftSearch(index.toString(), q.toString(), params);
    commandListener.searchFinished(index.toString(), q, null, result);
    return result;
  }

  @Override
  public AggregationResult aggregate(AggregationBuilder q) {
    commandListener.aggregateStarted(index.toString(), q);
    AggregationResult result = null;
    try {
      result = search.ftAggregate(index.toString(), q);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.aggregateFinished(index.toString(), q, result);
    }
    return result;
  }

  @Override
  public String cursorDelete(long cursorId) {
    commandListener.cursorDeleteStarted(index.toString(), cursorId);
    String result = null;
    try {
      result = search.ftCursorDel(index.toString(), cursorId);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.cursorDeleteFinished(index.toString(), cursorId, result);
    }
    return result;
  }

  @Override
  public AggregationResult cursorRead(long cursorId, int count) {
    commandListener.cursorReadStarted(index.toString(), cursorId, count);
    AggregationResult result = null;
    try {
      result = search.ftCursorRead(index.toString(), cursorId, count);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.cursorReadFinished(index.toString(), cursorId, count, result);
    }
    return result;
  }

  @Override
  public String explain(Query q) {
    commandListener.explainStarted(index.toString(), q);
    String result = null;
    try {
      result = search.ftExplain(index.toString(), q);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.explainFinished(index.toString(), q, result);
    }
    return result;
  }

  @Override
  public Map<String, Object> getInfo() {
    commandListener.infoStarted(index.toString());
    Map<String, Object> result = Map.of();
    try {
      result = search.ftInfo(index.toString());
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.infoFinished(index.toString(), result);
    }
    return result;
  }

  @Override
  public String dropIndex() {
    commandListener.dropIndexStarted(index.toString());
    String result = null;
    try {
      result = search.ftDropIndex(index.toString());
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.dropIndexFinished(index.toString(), result);
    }
    return result;
  }

  @Override
  public String dropIndexAndDocuments() {
    commandListener.dropIndexAndDocumentsStarted(index.toString());
    String result = null;
    try {
      result = search.ftDropIndexDD(index.toString());
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.dropIndexAndDocumentsFinished(index.toString(), result);
    }
    return result;
  }

  @Override
  public Long addSuggestion(String key, String suggestion) {
    return addSuggestion(key, suggestion, 1.0);
  }

  @Override
  public Long addSuggestion(String key, String suggestion, double score) {
    commandListener.addSuggestionStarted(index.toString(), key, suggestion, score);
    long result = 0;
    try {
      result = search.ftSugAdd(key, suggestion, score);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.addSuggestionFinished(index.toString(), key, suggestion, score, result);
    }
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
      List<Suggestion> list = List.of();
      try {
        list = suggestions.stream().map(suggestion -> {
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
      } catch (Exception e) {
        throw e;
      } finally {
        commandListener.getSuggestionFinished(index.toString(), key, prefix, options, list);
      }
      return list;
    } else {
      List<String> suggestions = search.ftSugGet(key, prefix, options.isFuzzy(), options.getLimit());
      List<Suggestion> list = List.of();
      try {
        list = suggestions.stream().map(suggestion -> {
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
      } catch (Exception e) {
        throw e;
      } finally {
        commandListener.getSuggestionFinished(index.toString(), key, prefix, options, list);
      }
      return list;
    }
  }

  @Override
  public Boolean deleteSuggestion(String key, String entry) {
    commandListener.deleteSuggestionStarted(index.toString(), key, entry);
    boolean result = false;
    try {
      result = search.ftSugDel(key, entry);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.deleteSuggestionFinished(index.toString(), key, entry, result);
    }
    return result;
  }

  @Override
  public Long getSuggestionLength(String key) {
    commandListener.getSuggestionLengthStarted(index.toString(), key);
    long result = 0;
    try {
      result = search.ftSugLen(key);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.getSuggestionLengthFinished(index.toString(), key, result);
    }
    return result;
  }

  @Override
  public String alterIndex(SchemaField... fields) {
    commandListener.alterIndexStarted(index.toString(), fields);
    String result = null;
    try {
      result = search.ftAlter(index.toString(), fields);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.alterIndexFinished(index.toString(), fields, result);
    }
    return result;
  }

  @Override
  public String setConfig(String option, String value) {
    commandListener.setConfigStarted(index.toString(), option, value);
    String result = null;
    try {
      result = search.ftConfigSet(option, value);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.setConfigFinished(index.toString(), option, value, result);
    }
    return result;
  }

  @Override
  public Map<String, Object> getConfig(String option) {
    commandListener.getConfigStarted(index.toString(), option);
    Map<String, Object> result = Map.of();
    try {
      result = search.ftConfigGet(option);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.getConfigFinished(index.toString(), option, result);
    }
    return result;
  }

  @Override
  public Map<String, Object> getIndexConfig(String option) {
    commandListener.getIndexConfigStarted(index.toString(), option);
    Map<String, Object> result = Map.of();
    try {
      result = search.ftConfigGet(index.toString(), option);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.getIndexConfigFinished(index.toString(), option, result);
    }
    return result;
  }

  @Override
  public String addAlias(String name) {
    commandListener.addAliasStarted(index.toString(), name);
    String result = null;
    try {
      result = search.ftAliasAdd(name, index.toString());
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.addAliasFinished(index.toString(), name, result);
    }
    return result;
  }

  @Override
  public String updateAlias(String name) {
    commandListener.updateAliasStarted(index.toString(), name);
    String result = null;
    try {
      result = search.ftAliasUpdate(name, index.toString());
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.updateAliasFinished(index.toString(), name, result);
    }
    return result;
  }

  @Override
  public String deleteAlias(String name) {
    commandListener.deleteAliasStarted(index.toString(), name);
    String result = null;
    try {
      result = search.ftAliasDel(name);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.deleteAliasFinished(index.toString(), name, result);
    }
    return result;
  }

  @Override
  public String updateSynonym(String synonymGroupId, String... terms) {
    commandListener.updateSynonymStarted(index.toString(), synonymGroupId, terms);
    String result = null;
    try {
      result = search.ftSynUpdate(index.toString(), synonymGroupId, terms);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.updateSynonymFinished(index.toString(), synonymGroupId, terms, result);
    }
    return result;
  }

  @Override
  public Map<String, List<String>> dumpSynonym() {
    commandListener.dumpSynonymStarted(index.toString());
    Map<String, List<String>> result = Map.of();
    try {
      result = search.ftSynDump(index.toString());
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.dumpSynonymFinished(index.toString(), result);
    }
    return result;
  }

  @Override
  public Set<String> tagVals(String field) {
    commandListener.tagValsStarted(index.toString(), field);
    Set<String> result = Set.of();
    try {
      result = search.ftTagVals(index.toString(), field);
    } catch (Exception e) {
      throw e;
    } finally {
      commandListener.tagValsFinished(index.toString(), field, result);
    }
    return result;
  }

}
