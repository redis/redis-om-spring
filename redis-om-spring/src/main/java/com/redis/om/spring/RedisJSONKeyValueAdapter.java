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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.annotation.Reference;
import org.springframework.data.annotation.Version;
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
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
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
  public RedisJSONKeyValueAdapter( //
    RedisOperations<?, ?> redisOps, //
    RedisModulesOperations<?> rmo, //
    RedisMappingContext mappingContext, //
    RediSearchIndexer keyspaceToIndexMap, //
    GsonBuilder gsonBuilder, //
    FeatureExtractor featureExtractor, //
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

    processVersion(key, item);
    auditor.processEntity(key, item);
    featureExtractor.processEntity(item);
    Optional<Long> maybeTtl = getTTLForEntity(item);

    ops.set(key, item);
    processReferences(key, item);

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
      ops.del(getKey(keyspace, id), Path2.ROOT_PATH);
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

  @SuppressWarnings("unchecked")
  private void processReferences(String key, Object item) {
    List<Field> fields = ObjectUtils.getFieldsWithAnnotation(item.getClass(), Reference.class);
    if (!fields.isEmpty()) {
      JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
      PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(item);
      fields.forEach(f -> {
        var referencedValue = accessor.getPropertyValue(f.getName());
        if (referencedValue != null) {
          if (referencedValue instanceof Collection<?> referenceValues) {
            List<String> referenceKeys = new ArrayList<>();
            referenceValues.forEach(r -> {
              Object id = ObjectUtils.getIdFieldForEntity(r);
              if (id != null) {
                String referenceKey = indexer.getKeyspaceForEntityClass(r.getClass()) + id;
                referenceKeys.add(referenceKey);
              }
            });
            ops.set(key, referenceKeys, Path2.of("$." + f.getName()));
          } else {
            Object id = ObjectUtils.getIdFieldForEntity(referencedValue);
            if (id != null) {
              String referenceKey = indexer.getKeyspaceForEntityClass(f.getType()) + id;
              ops.set(key, referenceKey, Path2.of("$." + f.getName()));
            }
          }
        }
      });
    }
  }

  private void processVersion(String key, Object item) {
    List<Field> fields = ObjectUtils.getFieldsWithAnnotation(item.getClass(), Version.class);
    if (fields.size() == 1) {
      BeanWrapper wrapper = new BeanWrapperImpl(item);
      Field versionField = fields.get(0);
      String property = versionField.getName();
      if ((versionField.getType() == Integer.class || isPrimitiveOfType(versionField.getType(), Integer.class)) ||
         (versionField.getType() == Long.class || isPrimitiveOfType(versionField.getType(), Long.class))) {
        Number version = (Number) wrapper.getPropertyValue(property);
        Number dbVersion = getEntityVersion(key, property);

        if (dbVersion != null && version != null && dbVersion.longValue() != version.longValue()) {
          throw new OptimisticLockingFailureException(
              String.format("Cannot insert/update entity %s with version %s as it already exists", item,
                  version));
        } else {
          Number nextVersion = version == null ? 0 : version.longValue() + 1;
          try {
            wrapper.setPropertyValue(property, nextVersion);
          } catch (NotWritablePropertyException nwpe) {
            versionField.setAccessible(true);
            try {
              versionField.set(item, nextVersion);
            } catch (IllegalAccessException iae) {
              // throw the original exception?
              throw new RuntimeException(nwpe);
            }
          }
        }
      }
    }
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
          if (fld != null) {
            ttlGetter = ObjectUtils.getGetterForField(entityClass, fld);
            long ttlPropertyValue = ((Number) ReflectionUtils.invokeMethod(ttlGetter, entity)).longValue();

            TimeToLive ttl = fld.getAnnotation(TimeToLive.class);
            if (!ttl.unit().equals(TimeUnit.SECONDS)) {
              return Optional.of(TimeUnit.SECONDS.convert(ttlPropertyValue, ttl.unit()));
            } else {
              return Optional.of(ttlPropertyValue);
            }
          } else {
            return Optional.empty();
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

  @SuppressWarnings("unchecked")
  private Number getEntityVersion(String key, String versionProperty) {
    JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
    Class<?> type = new TypeToken<Long[]>() {}.getRawType();
    Long[] dbVersionArray = (Long[]) ops.get(key, type, Path2.of("$." + versionProperty));
    return dbVersionArray != null ? dbVersionArray[0] : null;
  }
}
