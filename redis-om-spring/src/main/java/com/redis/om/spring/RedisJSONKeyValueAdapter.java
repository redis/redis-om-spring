package com.redis.om.spring;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.audit.EntityAuditor;
import com.redis.om.spring.convert.RedisOMCustomConversions;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.util.ObjectUtils;
import com.redis.om.spring.vectorize.FeatureExtractor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.*;
import org.springframework.data.redis.core.TimeToLive;
import redis.clients.jedis.search.Query;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.search.Document;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.annotation.Reference;
import org.springframework.data.annotation.Version;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.lang.Nullable;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.util.SafeEncoder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.redis.om.spring.util.ObjectUtils.getKey;
import static com.redis.om.spring.util.ObjectUtils.isPrimitiveOfType;
public class RedisJSONKeyValueAdapter extends RedisKeyValueAdapter {

  private static final Log logger = LogFactory.getLog(RedisJSONKeyValueAdapter.class);

  private final JSONOperations<?> redisJSONOperations;
  private final RedisOperations<?, ?> redisOperations;
  private final RedisMappingContext mappingContext;
  private final RedisModulesOperations<String> modulesOperations;
  private final RediSearchIndexer indexer;
  private final GsonBuilder gsonBuilder;
  private final EntityAuditor auditor;
  private final FeatureExtractor featureExtractor;
  private final RedisOMProperties redisOMProperties;

  private final ReferenceProcessor referenceProcessor;
  private final VersionProcessor versionProcessor;

  public RedisJSONKeyValueAdapter(
          RedisOperations<?, ?> redisOps,
          RedisModulesOperations<?> rmo,
          RedisMappingContext mappingContext,
          RediSearchIndexer keyspaceToIndexMap,
          GsonBuilder gsonBuilder,
          FeatureExtractor featureExtractor,
          RedisOMProperties redisOMProperties
  ) {
    super(redisOps, mappingContext, new RedisOMCustomConversions());

    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.redisJSONOperations = modulesOperations.opsForJSON();
    this.redisOperations = redisOps;
    this.mappingContext = mappingContext;
    this.indexer = keyspaceToIndexMap;
    this.auditor = new EntityAuditor(this.redisOperations);
    this.gsonBuilder = gsonBuilder;
    this.featureExtractor = featureExtractor;
    this.redisOMProperties = redisOMProperties;

    this.referenceProcessor = new ReferenceProcessor(redisJSONOperations,indexer);
    this.versionProcessor = new VersionProcessor(redisJSONOperations);
  }

  @Override
  public Object put(Object id, Object item, String keyspace) {
    logger.debug(String.format("%s, %s, %s", id, item, keyspace));

    @SuppressWarnings("unchecked")
    JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;

    String key = getKey(keyspace, id);

    referenceProcessor.processReferences(key, item);
    versionProcessor.processVersion(key, item);
    Optional<Long> maybeTtl = getTTLForEntity(item);

    ops.set(key, item);
    processReferences(key, item);

    redisOperations.execute((RedisCallback<Object>) connection -> {
      maybeTtl.ifPresent(aLong -> connection.keyCommands().expire(toBytes(key), aLong));
      return null;
    });

    return item;
  }

  @Nullable
  @Override
  public <T> T get(Object id, String keyspace, Class<T> type) {
    return get(getKey(keyspace, id), type);
  }

  @Nullable
  public <T> T get(String key, Class<T> type) {
    @SuppressWarnings("unchecked")
    JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
    return ops.get(key, type);
  }

  @Override
  public <T> List<T> getAllOf(String keyspace, Class<T> type, long offset, int rows) {
    Optional<String> maybeSearchIndex = indexer.getIndexName(keyspace);

    List<T> result = List.of();
    if (maybeSearchIndex.isPresent()) {
      SearchOperations<String> searchOps = modulesOperations.opsForSearch(maybeSearchIndex.get());
      Query query = new Query("*");
      offset = Math.max(0, offset);
      int limit = rows;
      if (limit <= 0) {
        limit = redisOMProperties.getRepository().getQuery().getLimit();
      }
      query.limit(Math.toIntExact(offset), limit);
      SearchResult searchResult = searchOps.search(query);
      Gson gson = gsonBuilder.create();
      result = searchResult.getDocuments().stream()
              .map(d -> gson.fromJson(SafeEncoder.encode((byte[])d.get("$")), type)) //
              .toList();
    }

    return result;
  }

  public <T> List<String> getAllKeys(String keyspace, Class<T> type) {
    Optional<String> maybeSearchIndex = indexer.getIndexName(keyspace);

    List<String> keys = List.of();
    if (maybeSearchIndex.isPresent()) {
      SearchOperations<String> searchOps = modulesOperations.opsForSearch(maybeSearchIndex.get());
      Optional<Field> maybeIdField = ObjectUtils.getIdFieldForEntityClass(type);
      String idField = maybeIdField.map(Field::getName).orElse("id");

      Query query = new Query("*");
      query.returnFields(idField);
      SearchResult searchResult = searchOps.search(query);

      keys = searchResult.getDocuments().stream()
              .map(Document::getId) //
              .toList();
    }

    return keys;
  }

  @Override
  public <T> T delete(Object id, String keyspace, Class<T> type) {
    @SuppressWarnings("unchecked")
    JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
    T entity = get(id, keyspace, type);
    if (entity != null) {
      ops.del(getKey(keyspace, id), Path.ROOT_PATH);
    }
    return entity;
  }

  @Override
  public void deleteAllOf(String keyspace) {
    Class<?> type = indexer.getEntityClassForKeyspace(keyspace);
    Optional<String> maybeSearchIndex = indexer.getIndexName(keyspace);
    if (maybeSearchIndex.isPresent()) {
      SearchOperations<String> searchOps = modulesOperations.opsForSearch(maybeSearchIndex.get());
      searchOps.dropIndexAndDocuments();
      indexer.createIndexFor(type);
    }
  }

  @Override
  public long count(String keyspace) {
    long count = 0L;
    Optional<String> maybeIndexName = indexer.getIndexName(keyspace);
    if (maybeIndexName.isPresent()) {
      SearchOperations<String> search = modulesOperations.opsForSearch(maybeIndexName.get());
      // FT.SEARCH index * LIMIT 0 0
      Query query = new Query("*");
      query.limit(0, 0);

      SearchResult result = search.search(query);

      count = result.getTotalResults();
    }
    return count;
  }

  @Override
  public boolean contains(Object id, String keyspace) {
    Boolean exists = redisOperations
            .execute((RedisCallback<Boolean>) connection -> connection.keyCommands().exists(toBytes(getKey(keyspace, id))));

    return exists != null && exists;
  }

  private void processReferences(String key, Object item) {
    referenceProcessor.processReferences(key, item);
  }

  private void processVersion(String key, Object item) {
    versionProcessor.processVersion(key, item);
  }

  private Optional<Long> getTTLForEntity(Object entity) {
    Class<?> entityClass = entity.getClass();
    Class<?> entityClassKey;
    try {
      entityClassKey = ClassLoader.getSystemClassLoader().loadClass(entity.getClass().getTypeName());
    } catch (ClassNotFoundException e) {
      entityClassKey = entity.getClass();
    }

    KeyspaceConfiguration keyspaceConfig = mappingContext.getMappingConfiguration().getKeyspaceConfiguration();
    if (keyspaceConfig.hasSettingsFor(entityClassKey)) {
      var settings = keyspaceConfig.getKeyspaceSettings(entityClassKey);
      if (StringUtils.hasText(settings.getTimeToLivePropertyName())) {
        Method ttlGetter;
        try {
          Field fld = ReflectionUtils.findField(entityClass, settings.getTimeToLivePropertyName());
          ttlGetter = ObjectUtils.getGetterForField(entityClass, fld);
          Long ttlPropertyValue = ((Number) ReflectionUtils.invokeMethod(ttlGetter, entity)).longValue();

          TimeToLive ttl = fld.getAnnotation(TimeToLive.class);
          if (!ttl.unit().equals(TimeUnit.SECONDS)) {
            return Optional.of(TimeUnit.SECONDS.convert(ttlPropertyValue, ttl.unit()));
          } else {
            return Optional.of(ttlPropertyValue);
          }
        } catch (SecurityException | IllegalArgumentException e) {
          return Optional.empty();
        }
      } else if (settings.getTimeToLive() != null && settings.getTimeToLive() > 0) {
        return Optional.of(settings.getTimeToLive());
      }
    }
    return Optional.empty();
  }

  private Number getEntityVersion(String key, String versionProperty) {
    JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
    Class<?> type = new TypeToken<Long[]>() {}.getRawType();
    Long[] dbVersionArray = (Long[]) ops.get(key, type, Path.of("$." + versionProperty));
    return dbVersionArray != null ? dbVersionArray[0] : null;
  }
}


