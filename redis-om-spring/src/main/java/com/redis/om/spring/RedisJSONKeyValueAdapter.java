package com.redis.om.spring;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redis.om.spring.convert.RedisOMCustomConversions;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.util.ObjectUtils;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

public class RedisJSONKeyValueAdapter extends RedisKeyValueAdapter {
  private static final Log logger = LogFactory.getLog(RedisJSONKeyValueAdapter.class);
  private final JSONOperations<?> redisJSONOperations;
  private final RedisOperations<?, ?> redisOperations;
  private final RedisMappingContext mappingContext;
  private final RedisModulesOperations<String> modulesOperations;
  private final RediSearchIndexer indexer;
  private final Gson gson;

  /**
   * Creates new {@link RedisKeyValueAdapter} with default
   * {@link RedisCustomConversions}.
   *
   * @param redisOps            must not be {@literal null}.
   * @param rmo                 must not be {@literal null}.
   * @param mappingContext      must not be {@literal null}.
   * @param keyspaceToIndexMap  must not be {@literal null}.
   */
  @SuppressWarnings("unchecked")
  public RedisJSONKeyValueAdapter(RedisOperations<?, ?> redisOps, RedisModulesOperations<?> rmo,
      RedisMappingContext mappingContext, RediSearchIndexer keyspaceToIndexMap, GsonBuilder gsonBuilder) {
    super(redisOps, mappingContext, new RedisOMCustomConversions());
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.redisJSONOperations = modulesOperations.opsForJSON();
    this.redisOperations = redisOps;
    this.mappingContext = mappingContext;
    this.indexer = keyspaceToIndexMap;
    this.gson = gsonBuilder.create();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.springframework.data.keyvalue.core.KeyValueAdapter#put(java.lang.Object,
   * java.lang.Object, java.lang.String)
   */
  @Override
  public Object put(Object id, Object item, String keyspace) {
    logger.debug(String.format("%s, %s, %s", id, item, keyspace));
    @SuppressWarnings("unchecked")
    JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;

    String key = getKey(keyspace, id);

    processAuditAnnotations(key, item);
    Optional<Long> maybeTtl = getTTLForEntity(item);

    ops.set(key, item);

    redisOperations.execute((RedisCallback<Object>) connection -> {

      maybeTtl.ifPresent(aLong -> connection.keyCommands().expire(toBytes(key), aLong));

      return null;
    });

    return item;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.springframework.data.keyvalue.core.KeyValueAdapter#get(java.lang.Object,
   * java.lang.String, java.lang.Class)
   */
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

  /**
   * Get all elements for given keyspace.
   *
   * @param keyspace the keyspace to fetch entities from.
   * @param type     the desired target type.
   * @param offset   index value to start reading.
   * @param rows     maximum number of entities to return.
   * @return never {@literal null}.
   */
  @Override
  public <T> List<T> getAllOf(String keyspace, Class<T> type, long offset, int rows) {
    Optional<String> maybeSearchIndex = indexer.getIndexName(keyspace);

    List<T> result = List.of();
    if (maybeSearchIndex.isPresent()) {
      SearchOperations<String> searchOps = modulesOperations.opsForSearch(maybeSearchIndex.get());
      Query query = new Query("*");
      offset = Math.max(0, offset);
      if (rows > 0) {
        query.limit(Math.toIntExact(offset), rows);
      }
      SearchResult searchResult = searchOps.search(query);

      result = searchResult.getDocuments().stream()
          .map(d -> gson.fromJson(d.get("$").toString(), type)) //
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.springframework.data.keyvalue.core.AbstractKeyValueAdapter#delete(java.
   * lang.Object, java.lang.String, java.lang.Class)
   */
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

  /*
   * (non-Javadoc)
   *
   * @see
   * org.springframework.data.keyvalue.core.KeyValueAdapter#deleteAllOf(java.lang.
   * String)
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.data.keyvalue.core.KeyValueAdapter#count(java.lang.
   * String)
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.springframework.data.keyvalue.core.KeyValueAdapter#contains(java.lang.
   * Object, java.lang.String)
   */
  @Override
  public boolean contains(Object id, String keyspace) {
    Boolean exists = redisOperations
        .execute((RedisCallback<Boolean>) connection -> connection.keyCommands().exists(toBytes(getKey(keyspace, id))));

    return exists != null && exists;
  }

  private void processAuditAnnotations(String key, Object item) {
    boolean isNew = (boolean) redisOperations
        .execute((RedisCallback<Object>) connection -> !connection.keyCommands().exists(toBytes(key)));

    var auditClass = isNew ? CreatedDate.class : LastModifiedDate.class;

    List<Field> fields = ObjectUtils.getFieldsWithAnnotation(item.getClass(), auditClass);
    if (!fields.isEmpty()) {
      PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(item);
      fields.forEach(f -> {
        if (f.getType() == Date.class) {
          accessor.setPropertyValue(f.getName(), new Date(System.currentTimeMillis()));
        } else if (f.getType() == LocalDateTime.class) {
          accessor.setPropertyValue(f.getName(), LocalDateTime.now());
        } else if (f.getType() == LocalDate.class) {
          accessor.setPropertyValue(f.getName(), LocalDate.now());
        }
      });
    }
  }

  protected String getKey(String keyspace, Object id) {
    return String.format("%s:%s", keyspace, id);
  }

  private Optional<Long> getTTLForEntity(Object entity) {
    KeyspaceConfiguration keyspaceConfig = mappingContext.getMappingConfiguration().getKeyspaceConfiguration();
    if (keyspaceConfig.hasSettingsFor(entity.getClass())) {
      var settings = keyspaceConfig.getKeyspaceSettings(entity.getClass());

      if (StringUtils.hasText(settings.getTimeToLivePropertyName())) {
        Method ttlGetter;
        try {
          Field fld = ReflectionUtils.findField(entity.getClass(), settings.getTimeToLivePropertyName());
          ttlGetter = ObjectUtils.getGetterForField(entity.getClass(), fld);
          Long ttlPropertyValue = ((Number) ReflectionUtils.invokeMethod(ttlGetter, entity)).longValue();

          ReflectionUtils.invokeMethod(ttlGetter, entity);

          if (ttlPropertyValue != null) {
            TimeToLive ttl = fld.getAnnotation(TimeToLive.class);
            if (!ttl.unit().equals(TimeUnit.SECONDS)) {
              return Optional.of(TimeUnit.SECONDS.convert(ttlPropertyValue, ttl.unit()));
            } else {
              return Optional.of(ttlPropertyValue);
            }
          }
        } catch (SecurityException | IllegalArgumentException e) {
          return Optional.empty();
        }
      } else if (settings != null && settings.getTimeToLive() != null && settings.getTimeToLive() > 0) {
        return Optional.of(settings.getTimeToLive());
      }
    }
    return Optional.empty();
  }
}
