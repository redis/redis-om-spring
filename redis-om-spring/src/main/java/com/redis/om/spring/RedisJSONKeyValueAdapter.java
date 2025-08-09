package com.redis.om.spring;

import static com.redis.om.spring.util.ObjectUtils.isPrimitiveOfType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.annotation.Reference;
import org.springframework.data.annotation.Version;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.redis.core.mapping.RedisPersistentProperty;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.audit.EntityAuditor;
import com.redis.om.spring.convert.RedisOMCustomConversions;
import com.redis.om.spring.id.IdentifierFilter;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.mapping.RedisEnhancedPersistentEntity;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.util.ObjectUtils;
import com.redis.om.spring.vectorize.Embedder;

import jakarta.persistence.IdClass;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.util.SafeEncoder;

/**
 * JSON document-focused {@link RedisKeyValueAdapter} implementation that leverages RedisJSON
 * for storing and retrieving entities as JSON documents in Redis.
 * <p>
 * This adapter provides specialized functionality for Redis OM Spring's JSON document storage,
 * including:
 * <ul>
 * <li>RedisJSON integration for native JSON document operations</li>
 * <li>RediSearch integration for full-text and vector search on JSON documents</li>
 * <li>Entity embedding processing via {@link Embedder}</li>
 * <li>Enhanced entity auditing capabilities</li>
 * <li>Composite ID support for complex entity keys</li>
 * <li>Optimized batch operations for large JSON document datasets</li>
 * </ul>
 * <p>
 * Unlike {@link RedisEnhancedKeyValueAdapter} which stores entities as Redis Hash structures,
 * this adapter stores entities as JSON documents using RedisJSON commands, enabling
 * more complex nested data structures and JSON path operations.
 *
 * @author Redis OM Spring Development Team
 * @see RedisKeyValueAdapter
 * @see RedisModulesOperations
 * @see RediSearchIndexer
 * @see Embedder
 * @see JSONOperations
 * @since 1.0
 */
public class RedisJSONKeyValueAdapter extends RedisKeyValueAdapter {
  private static final Log logger = LogFactory.getLog(RedisJSONKeyValueAdapter.class);
  private final JSONOperations<?> redisJSONOperations;
  private final RedisOperations<?, ?> redisOperations;
  private final RedisMappingContext mappingContext;
  private final RedisModulesOperations<String> modulesOperations;
  private final RediSearchIndexer indexer;
  private final GsonBuilder gsonBuilder;
  private final EntityAuditor auditor;
  private final Embedder embedder;
  private final RedisOMProperties redisOMProperties;

  /**
   * Creates new {@link RedisJSONKeyValueAdapter} with default
   * {@link RedisCustomConversions}.
   *
   * @param redisOps          must not be {@literal null}.
   * @param rmo               must not be {@literal null}.
   * @param mappingContext    must not be {@literal null}.
   * @param indexer           must not be {@literal null}.
   * @param gsonBuilder       the GSON builder for JSON serialization, must not be {@literal null}.
   * @param embedder          the embedder for processing entity embeddings, must not be {@literal null}.
   * @param redisOMProperties the Redis OM configuration properties, must not be {@literal null}.
   */
  @SuppressWarnings(
    "unchecked"
  )
  public RedisJSONKeyValueAdapter( //
      RedisOperations<?, ?> redisOps, //
      RedisModulesOperations<?> rmo, //
      @Qualifier(
        "redisEnhancedMappingContext"
      ) RedisMappingContext mappingContext, //
      RediSearchIndexer indexer, //
      GsonBuilder gsonBuilder, //
      Embedder embedder, //
      RedisOMProperties redisOMProperties) {
    super(redisOps, mappingContext, new RedisOMCustomConversions());
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.redisJSONOperations = modulesOperations.opsForJSON();
    this.redisOperations = redisOps;
    this.mappingContext = mappingContext;
    this.indexer = indexer;
    this.auditor = new EntityAuditor(this.redisOperations);
    this.gsonBuilder = gsonBuilder;
    this.embedder = embedder;
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
    @SuppressWarnings(
      "unchecked"
    ) JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;

    String stringId = validateKeyForWriting(id, item);

    String key = createKeyAsString(keyspace, stringId);

    processVersion(key, item);
    auditor.processEntity(key, item);
    embedder.processEntity(item);
    Optional<Long> maybeTtl = getTTLForEntity(item);

    ops.set(key, item);
    processReferences(key, item);

    redisOperations.execute((RedisCallback<Object>) connection -> {
      maybeTtl.ifPresent(ttl -> {
        if (ttl > 0)
          connection.keyCommands().expire(toBytes(key), ttl);
      });
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
    String stringId = asStringValue(id);
    String key = createKeyAsString(keyspace, stringId);
    return get(key, type);
  }

  /**
   * Retrieves an entity of the specified type using the provided Redis key.
   * <p>
   * This method performs a direct JSON document retrieval using RedisJSON operations
   * without the need for keyspace and ID construction.
   *
   * @param key  the complete Redis key for the JSON document
   * @param type the target entity type to deserialize to
   * @param <T>  the entity type parameter
   * @return the deserialized entity instance, or {@code null} if not found
   */
  @Nullable
  public <T> T get(String key, Class<T> type) {
    @SuppressWarnings(
      "unchecked"
    ) JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
    T entity = ops.get(key, type);
    if (entity != null) {
      // Use optimized method if we can get the persistent entity
      RedisPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(type);
      if (persistentEntity instanceof RedisEnhancedPersistentEntity) {
        ((RedisEnhancedPersistentEntity<?>) persistentEntity).populateRedisKey(entity, key);
      } else {
        // Fallback to utility method
        ObjectUtils.populateRedisKey(entity, key);
      }
    }
    return entity;
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
    String searchIndex = indexer.getIndexName(keyspace);
    SearchOperations<String> searchOps = modulesOperations.opsForSearch(searchIndex);
    Query query = new Query("*");
    offset = Math.max(0, offset);
    int limit = rows;
    if (limit <= 0) {
      limit = redisOMProperties.getRepository().getQuery().getLimit();
    }
    query.limit(Math.toIntExact(offset), limit);
    SearchResult searchResult = searchOps.search(query);
    Gson gson = gsonBuilder.create();

    // Get persistent entity once for all results
    RedisPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(type);
    boolean useOptimized = persistentEntity instanceof RedisEnhancedPersistentEntity;

    return searchResult.getDocuments().stream().map(d -> {
      T entity = gson.fromJson(SafeEncoder.encode((byte[]) d.get("$")), type);
      if (useOptimized) {
        return ((RedisEnhancedPersistentEntity<?>) persistentEntity).populateRedisKey(entity, d.getId());
      } else {
        return ObjectUtils.populateRedisKey(entity, d.getId());
      }
    }).toList();
  }

  /**
   * Retrieves all entity keys for the given keyspace and type using RediSearch.
   * <p>
   * This method performs a wildcard search on the RediSearch index to find all JSON documents
   * and extracts their Redis key identifiers. It's optimized to return only the key identifiers
   * rather than full document content for better performance.
   *
   * @param keyspace the Redis keyspace to search in
   * @param type     the entity type to retrieve keys for
   * @param <T>      the type parameter for the entity class
   * @return a list of Redis key strings for all entities in the keyspace
   */
  public <T> List<String> getAllKeys(String keyspace, Class<T> type) {
    String searchIndex = indexer.getIndexName(keyspace);
    SearchOperations<String> searchOps = modulesOperations.opsForSearch(searchIndex);
    Optional<Field> maybeIdField = ObjectUtils.getIdFieldForEntityClass(type);
    String idField = maybeIdField.map(Field::getName).orElse("id");

    Query query = new Query("*");
    query.returnFields(idField);
    SearchResult searchResult = searchOps.search(query);

    return searchResult.getDocuments().stream() //
        .map(Document::getId) //
        .toList();
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
    @SuppressWarnings(
      "unchecked"
    ) JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
    String stringId = asStringValue(id);
    T entity = get(stringId, keyspace, type);
    if (entity != null) {
      String key = createKeyAsString(keyspace, stringId);
      ops.del(key, Path2.ROOT_PATH);
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
    String searchIndex = indexer.getIndexName(keyspace);
    SearchOperations<String> searchOps = modulesOperations.opsForSearch(searchIndex);
    if (redisOMProperties.getRepository().isDropAndRecreateIndexOnDeleteAll()) {
      searchOps.dropIndexAndDocuments();
      indexer.createIndexFor(type);
    } else {
      boolean moreRecords = true;
      while (moreRecords) {
        Query query = new Query("*");
        query.limit(0, redisOMProperties.getRepository().getDeleteBatchSize());
        query.setNoContent();
        SearchResult searchResult = searchOps.search(query);
        if (searchResult.getTotalResults() > 0) {
          List<byte[]> keys = searchResult.getDocuments().stream().map(k -> toBytes(k.getId())).toList();
          redisOperations.executePipelined((RedisCallback<Object>) connection -> {
            RedisKeyCommands keyCommands = connection.keyCommands();
            for (byte[] key : keys) {
              keyCommands.del(key);
            }
            return null;
          });
        } else {
          moreRecords = false;
        }
      }
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
    String indexName = indexer.getIndexName(keyspace);
    SearchOperations<String> search = modulesOperations.opsForSearch(indexName);
    var info = search.getInfo();
    return extractNumDocs(info);
  }

  private long extractNumDocs(Map<String, Object> info) {
    Object numDocsValue = info.get("num_docs");

    // Handle different return types from Redis
    if (numDocsValue instanceof String) {
      return Long.parseLong((String) numDocsValue);
    } else if (numDocsValue instanceof Number) {
      return ((Number) numDocsValue).longValue();
    } else {
      // Fallback to 0 if the value is null or unexpected type
      return 0L;
    }
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
    Boolean exists = redisOperations.execute((RedisCallback<Boolean>) connection -> connection.keyCommands().exists(
        toBytes(createKeyAsString(keyspace, id))));

    return exists != null && exists;
  }

  @SuppressWarnings(
    "unchecked"
  )
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
      if ((versionField.getType() == Integer.class || isPrimitiveOfType(versionField.getType(),
          Integer.class)) || (versionField.getType() == Long.class || isPrimitiveOfType(versionField.getType(),
              Long.class))) {
        Number version = (Number) wrapper.getPropertyValue(property);
        Number dbVersion = getEntityVersion(key, property);

        if (dbVersion != null && version != null && dbVersion.longValue() != version.longValue()) {
          throw new OptimisticLockingFailureException(String.format(
              "Cannot insert/update entity %s with version %s as it already exists", item, version));
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

    // Use the resolver if available for cross-class-loader compatibility
    KeyspaceConfiguration.KeyspaceSettings settings = null;
    if (mappingContext instanceof com.redis.om.spring.mapping.RedisEnhancedMappingContext) {
      var resolver = ((com.redis.om.spring.mapping.RedisEnhancedMappingContext) mappingContext).getKeyspaceResolver();
      if (resolver.hasSettingsFor(entityClassKey)) {
        settings = resolver.getKeyspaceSettings(entityClassKey);
      }
    } else {
      KeyspaceConfiguration keyspaceConfig = mappingContext.getMappingConfiguration().getKeyspaceConfiguration();
      if (keyspaceConfig.hasSettingsFor(entityClassKey)) {
        settings = keyspaceConfig.getKeyspaceSettings(entityClassKey);
      }
    }

    if (settings != null) {
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

  @SuppressWarnings(
    "unchecked"
  )
  private Number getEntityVersion(String key, String versionProperty) {
    JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
    Class<?> type = new TypeToken<Long[]>() {
    }.getRawType();
    Long[] dbVersionArray = (Long[]) ops.get(key, type, Path2.of("$." + versionProperty));
    return dbVersionArray != null ? dbVersionArray[0] : null;
  }

  /**
   * Creates a Redis key string by combining the keyspace and entity ID.
   * <p>
   * This method handles keyspace formatting and applies any configured identifier filters
   * to ensure proper key generation for JSON document storage. The resulting key follows
   * the pattern: {@code keyspace:id} or {@code keyspaceid} depending on keyspace format.
   *
   * @param keyspace the Redis keyspace (namespace) for the entity
   * @param id       the entity identifier
   * @return the complete Redis key as a string
   */
  public String createKeyAsString(String keyspace, Object id) {
    String format = keyspace.endsWith(":") ? "%s%s" : "%s:%s";

    // handle IdFilters
    var maybeIdentifierFilter = indexer.getIdentifierFilterFor(keyspace);
    if (maybeIdentifierFilter.isPresent()) {
      IdentifierFilter<String> filter = (IdentifierFilter<String>) maybeIdentifierFilter.get();
      id = filter.filter(id.toString());
    }
    return String.format(format, keyspace, id);
  }

  private String validateKeyForWriting(Object id, Object item) {
    // Get the mapping context's entity info
    RedisEnhancedPersistentEntity<?> entity = (RedisEnhancedPersistentEntity<?>) mappingContext
        .getRequiredPersistentEntity(item.getClass());

    // Handle composite IDs
    if (entity.isIdClassComposite()) {
      BeanWrapper wrapper = new DirectFieldAccessFallbackBeanWrapper(item);
      List<String> idParts = new ArrayList<>();

      for (RedisPersistentProperty idProperty : entity.getIdProperties()) {
        Object propertyValue = wrapper.getPropertyValue(idProperty.getName());
        if (propertyValue != null) {
          idParts.add(propertyValue.toString());
        }
      }

      return String.join(":", idParts);
    } else {
      // Regular single ID handling
      return getConverter().getConversionService().convert(id, String.class);
    }
  }

  private String asStringValue(Object value) {
    // For composite IDs used in @IdClass
    if (value != null) {
      // Get all persistent entities
      for (RedisPersistentEntity<?> entity : mappingContext.getPersistentEntities()) {
        // Find the entity that uses this ID class
        IdClass idClassAnn = entity.getType().getAnnotation(IdClass.class);
        if (idClassAnn != null && idClassAnn.value().equals(value.getClass())) {
          // Found the entity that uses this ID class
          BeanWrapper wrapper = new DirectFieldAccessFallbackBeanWrapper(value);
          RedisEnhancedPersistentEntity<?> enhancedEntity = (RedisEnhancedPersistentEntity<?>) entity;

          // Build composite key from ID properties in order
          List<String> idParts = new ArrayList<>();
          for (RedisPersistentProperty idProperty : enhancedEntity.getIdProperties()) {
            Object propertyValue = wrapper.getPropertyValue(idProperty.getName());
            if (propertyValue != null) {
              idParts.add(propertyValue.toString());
            }
          }
          return String.join(":", idParts);
        }
      }
    }

    return getConverter().getConversionService().convert(value, String.class);
  }
}
