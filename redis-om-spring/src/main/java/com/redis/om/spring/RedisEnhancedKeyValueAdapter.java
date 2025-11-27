package com.redis.om.spring;

import static com.redis.om.spring.util.ObjectUtils.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.PartialUpdate.PropertyUpdate;
import org.springframework.data.redis.core.PartialUpdate.UpdateCommand;
import org.springframework.data.redis.core.convert.RedisConverter;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.redis.core.mapping.RedisPersistentProperty;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.redis.om.spring.audit.EntityAuditor;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.convert.RedisOMCustomConversions;
import com.redis.om.spring.id.IdentifierFilter;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.mapping.RedisEnhancedMappingContext;
import com.redis.om.spring.mapping.RedisEnhancedPersistentEntity;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.vectorize.Embedder;

import jakarta.persistence.IdClass;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

/**
 * Enhanced {@link RedisKeyValueAdapter} implementation that extends Spring Data Redis capabilities
 * with Redis Stack modules support including RediSearch, RedisJSON, and vector similarity search.
 * <p>
 * This adapter provides advanced functionality for Redis OM Spring, including:
 * <ul>
 * <li>RediSearch integration for full-text and vector search</li>
 * <li>Entity embedding processing via {@link Embedder}</li>
 * <li>Enhanced entity auditing capabilities</li>
 * <li>Composite ID support for complex entity keys</li>
 * <li>Optimized batch operations for large datasets</li>
 * </ul>
 * <p>
 * The adapter works with both Redis Hash structures and JSON documents, automatically
 * handling conversions, indexing, and search operations through Redis Stack modules.
 *
 * @author Redis OM Spring Development Team
 * @see RedisKeyValueAdapter
 * @see RedisModulesOperations
 * @see RediSearchIndexer
 * @see Embedder
 * @since 1.0
 */
public class RedisEnhancedKeyValueAdapter extends RedisKeyValueAdapter {

  private static final Log logger = LogFactory.getLog(RedisEnhancedKeyValueAdapter.class);
  private final RedisOperations<?, ?> redisOperations;
  private final RedisConverter converter;
  private final RedisModulesOperations<String> modulesOperations;
  private final RediSearchIndexer indexer;
  private final EntityAuditor auditor;
  private final Embedder embedder;
  private final RedisOMProperties redisOMProperties;

  /**
   * Creates new {@link RedisKeyValueAdapter} with default
   * {@link RedisMappingContext} and default {@link RedisCustomConversions}.
   *
   * @param redisOps          must not be {@literal null}.
   * @param rmo               must not be {@literal null}.
   * @param indexer           must not be {@literal null}.
   * @param embedder          must not be {@literal null}.
   * @param redisOMProperties must not be {@literal null}.
   */
  public RedisEnhancedKeyValueAdapter( //
      RedisOperations<?, ?> redisOps, //
      RedisModulesOperations<?> rmo, //
      RediSearchIndexer indexer, //
      Embedder embedder, //
      RedisOMProperties redisOMProperties) {
    this(redisOps, rmo, new RedisEnhancedMappingContext(), indexer, embedder, redisOMProperties);
  }

  /**
   * Creates new {@link RedisKeyValueAdapter} with default
   * {@link RedisCustomConversions}.
   *
   * @param redisOps          must not be {@literal null}.
   * @param rmo               must not be {@literal null}.
   * @param mappingContext    must not be {@literal null}.
   * @param indexer           must not be {@literal null}.
   * @param embedder          must not be {@literal null}.
   * @param redisOMProperties must not be {@literal null}.
   */
  public RedisEnhancedKeyValueAdapter( //
      RedisOperations<?, ?> redisOps, //
      RedisModulesOperations<?> rmo, //
      RedisMappingContext mappingContext, //
      RediSearchIndexer indexer, //
      Embedder embedder, //
      RedisOMProperties redisOMProperties) {
    this(redisOps, rmo, mappingContext, new RedisOMCustomConversions(), indexer, embedder, redisOMProperties);
  }

  /**
   * Creates new {@link RedisKeyValueAdapter}.
   *
   * @param redisOps          must not be {@literal null}.
   * @param rmo               must not be {@literal null}.
   * @param mappingContext    must not be {@literal null}.
   * @param customConversions can be {@literal null}.
   * @param indexer           must not be {@literal null}.
   * @param embedder          must not be {@literal null}.
   * @param redisOMProperties must not be {@literal null}.
   */
  @SuppressWarnings(
    "unchecked"
  )
  public RedisEnhancedKeyValueAdapter( //
      RedisOperations<?, ?> redisOps, //
      RedisModulesOperations<?> rmo, //
      RedisMappingContext mappingContext, //
      @Nullable CustomConversions customConversions, //
      RediSearchIndexer indexer, //
      Embedder embedder, //
      RedisOMProperties redisOMProperties) {
    super(redisOps, mappingContext, customConversions);

    Assert.notNull(redisOps, "RedisOperations must not be null!");
    Assert.notNull(mappingContext, "RedisMappingContext must not be null!");

    MappingRedisOMConverter mappingConverter = new MappingRedisOMConverter(mappingContext, new ReferenceResolverImpl(
        redisOps));
    mappingConverter.setCustomConversions(customConversions == null ?
        new RedisOMCustomConversions() :
        customConversions);
    mappingConverter.afterPropertiesSet();

    this.converter = mappingConverter;
    this.redisOperations = redisOps;
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.indexer = indexer;
    this.auditor = new EntityAuditor(this.redisOperations);
    this.embedder = embedder;
    this.redisOMProperties = redisOMProperties;
  }

  private static String sanitizeKeyspace(String keyspace) {
    return keyspace.endsWith(":") ? keyspace.substring(0, keyspace.length() - 1) : keyspace;
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
    RedisData rdo;
    // Resolve dynamic keyspace from @IndexingOptions if present
    String resolvedKeyspace = resolveDynamicKeyspace(item.getClass(), keyspace);

    if (item instanceof RedisData redisData) {
      rdo = redisData;
    } else {
      String idAsString = validateKeyForWriting(id, item);
      byte[] redisKey = createKey(sanitizeKeyspace(resolvedKeyspace), idAsString);
      auditor.processEntity(redisKey, item);
      embedder.processEntity(item);

      rdo = new RedisData();
      converter.write(item, rdo);
      rdo.setId(idAsString);
      // Override the keyspace in RedisData with the resolved dynamic keyspace
      rdo.setKeyspace(sanitizeKeyspace(resolvedKeyspace));
    }

    redisOperations.executePipelined((RedisCallback<Object>) connection -> {
      byte[] objectKey = createKey(sanitizeKeyspace(rdo.getKeyspace()), rdo.getId());
      connection.keyCommands().del(objectKey);
      Map<byte[], byte[]> rawMap = rdo.getBucket().rawMap();
      connection.hashCommands().hMSet(objectKey, rawMap);

      if (willExpire(rdo)) {
        connection.keyCommands().expire(objectKey, rdo.getTimeToLive());
      }

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
    // Resolve dynamic keyspace from @IndexingOptions if present
    String resolvedKeyspace = resolveDynamicKeyspace(type, keyspace);
    String stringKeyspace = sanitizeKeyspace(resolvedKeyspace);

    byte[] binId = createKey(stringKeyspace, stringId);

    Map<byte[], byte[]> raw = redisOperations.execute((RedisCallback<Map<byte[], byte[]>>) connection -> connection
        .hashCommands().hGetAll(binId));

    if (CollectionUtils.isEmpty(raw)) {
      return null;
    }

    RedisData data = new RedisData(raw);
    data.setId(stringId);
    data.setKeyspace(stringKeyspace);

    T entity = readTimeToLiveIfSet(binId, converter.read(type, data));
    if (entity != null) {
      String redisKey = new String(binId);
      // Use optimized method if we can get the persistent entity
      RedisPersistentEntity<?> persistentEntity = converter.getMappingContext().getPersistentEntity(type);
      if (persistentEntity instanceof RedisEnhancedPersistentEntity) {
        ((RedisEnhancedPersistentEntity<?>) persistentEntity).populateRedisKey(entity, redisKey);
      } else {
        // Fallback to utility method
        com.redis.om.spring.util.ObjectUtils.populateRedisKey(entity, redisKey);
      }
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

  /**
   * Retrieves all entity IDs for the given keyspace and type using RediSearch.
   * <p>
   * This method performs a wildcard search on the RediSearch index to find all documents
   * and extracts their ID field values. It's optimized to return only the ID field
   * rather than full documents for better performance.
   *
   * @param keyspace the Redis keyspace to search in
   * @param type     the entity type to retrieve IDs for
   * @param <T>      the type parameter for the entity class
   * @return a list of string representations of all entity IDs in the keyspace
   */
  public <T> List<String> getAllIds(String keyspace, Class<T> type) {
    String searchIndex = indexer.getIndexName(keyspace);
    SearchOperations<String> searchOps = modulesOperations.opsForSearch(searchIndex);
    Optional<Field> maybeIdField = getIdFieldForEntityClass(type);
    String idField = maybeIdField.map(Field::getName).orElse("id");

    Query query = new Query("*");
    query.returnFields(idField);
    SearchResult searchResult = searchOps.search(query);

    return searchResult.getDocuments().stream().map(d -> documentToObject(d, type, (MappingRedisOMConverter) converter))
        .map(e -> maybeIdField.map(field -> getIdFieldForEntity(field, e)).orElse(null)).filter(Objects::nonNull).map(
            Object::toString).toList();
  }

  /**
   * Get all elements for given keyspace.
   *
   * @param keyspace the keyspace to fetch entities from.
   * @param type     the desired target type.
   * @param offset   index value to start reading.
   * @param rows     maximum number of entities to return.
   * @param <T>      the target type
   * @return never {@literal null}.
   * @since 2.5
   */
  @SuppressWarnings(
    "unchecked"
  )
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

    // Get persistent entity once for all results
    RedisPersistentEntity<?> persistentEntity = converter.getMappingContext().getPersistentEntity(type);
    boolean useOptimized = persistentEntity instanceof RedisEnhancedPersistentEntity;

    return (List<T>) searchResult.getDocuments().stream() //
        .map(d -> {
          Object entity = documentToObject(d, type, (MappingRedisOMConverter) converter);
          if (useOptimized) {
            return ((RedisEnhancedPersistentEntity<?>) persistentEntity).populateRedisKey(entity, d.getId());
          } else {
            return com.redis.om.spring.util.ObjectUtils.populateRedisKey(entity, d.getId());
          }
        }) //
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
    String stringId = asStringValue(id);
    // Resolve dynamic keyspace from @IndexingOptions if present
    String resolvedKeyspace = resolveDynamicKeyspace(type, keyspace);
    String stringKeyspace = sanitizeKeyspace(resolvedKeyspace);

    T o = get(stringId, stringKeyspace, type);

    if (o != null) {

      byte[] keyToDelete = createKey(stringKeyspace, stringId);

      redisOperations.execute((RedisCallback<Void>) connection -> {
        connection.keyCommands().unlink(keyToDelete);
        return null;
      });
    }

    return o;
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
    // Note: contains() doesn't have type parameter, so we can't resolve dynamic keyspace here
    // The caller should ensure the correct keyspace is passed
    Boolean exists = redisOperations.execute((RedisCallback<Boolean>) connection -> connection.keyCommands().exists(
        toBytes(getKey(keyspace, asStringValue(id)))));

    return exists != null && exists;
  }

  @Override
  public void update(PartialUpdate<?> update) {

    RedisPersistentEntity<?> entity = this.converter.getMappingContext().getRequiredPersistentEntity(update
        .getTarget());

    String keyspace = sanitizeKeyspace(entity.getKeySpace());
    Object id = update.getId();
    String stringId = asStringValue(id);

    byte[] redisKey = createKey(keyspace, stringId);

    RedisData rdo = new RedisData();
    this.converter.write(update, rdo);

    redisOperations.execute((RedisCallback<Void>) connection -> {

      RedisUpdateObject redisUpdateObject = new RedisUpdateObject(redisKey);

      for (PropertyUpdate pUpdate : update.getPropertyUpdates()) {

        String propertyPath = pUpdate.getPropertyPath();

        if (UpdateCommand.DEL.equals(pUpdate.getCmd()) || pUpdate.getValue() instanceof Collection || pUpdate
            .getValue() instanceof Map || (pUpdate.getValue() != null && pUpdate.getValue().getClass()
                .isArray()) || (pUpdate.getValue() != null && !converter.getConversionService().canConvert(pUpdate
                    .getValue().getClass(), byte[].class))) {

          redisUpdateObject = fetchDeletePathsFromHash(redisUpdateObject, propertyPath, connection);
        }
      }

      if (!redisUpdateObject.fieldsToRemove.isEmpty()) {
        connection.hashCommands().hDel(redisKey, redisUpdateObject.fieldsToRemove.toArray(
            new byte[redisUpdateObject.fieldsToRemove.size()][]));
      }

      if (!rdo.getBucket().isEmpty() &&  //
          ( //
      rdo.getBucket().size() > 1 || //
          (rdo.getBucket().size() == 1 && !rdo.getBucket().asMap().containsKey("_class")) //
      )) {
        connection.hashCommands().hMSet(redisKey, rdo.getBucket().rawMap());
      }

      if (update.isRefreshTtl()) {

        if (willExpire(rdo)) {
          connection.keyCommands().expire(redisKey, rdo.getTimeToLive());
        } else {
          connection.keyCommands().persist(redisKey);
        }
      }

      return null;
    });
  }

  private RedisUpdateObject fetchDeletePathsFromHash(RedisUpdateObject redisUpdateObject, String path,
      RedisConnection connection) {

    redisUpdateObject.addFieldToRemove(toBytes(path));
    byte[] value = connection.hashCommands().hGet(redisUpdateObject.targetKey, toBytes(path));

    if (value != null && value.length > 0) {
      return redisUpdateObject;
    }

    Set<byte[]> existingFields = connection.hashCommands().hKeys(redisUpdateObject.targetKey);

    for (byte[] field : existingFields) {

      if (asStringValue(field).startsWith(path + ".")) {
        redisUpdateObject.addFieldToRemove(field);
        connection.hashCommands().hGet(redisUpdateObject.targetKey, toBytes(field));
      }
    }

    return redisUpdateObject;
  }

  private String asStringValue(Object value) {
    if (value instanceof String valueAsString) {
      return valueAsString;
    }

    // For composite IDs used in @IdClass
    if (value != null) {
      // Get all persistent entities
      for (RedisPersistentEntity<?> entity : converter.getMappingContext().getPersistentEntities()) {
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

  private String validateKeyForWriting(Object id, Object item) {
    // Get the mapping context's entity info
    RedisEnhancedPersistentEntity<?> entity = (RedisEnhancedPersistentEntity<?>) converter.getMappingContext()
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
      return converter.getConversionService().convert(id, String.class);
    }
  }

  /**
   * Read back and set {@link TimeToLive} for the property.
   *
   * @param key    the key to read the TTL for.
   * @param target the target object.
   * @return the target object with the TTL set.
   */
  @Nullable
  private <T> T readTimeToLiveIfSet(@Nullable byte[] key, @Nullable T target) {

    if (target == null || key == null) {
      return target;
    }

    RedisPersistentEntity<?> entity = this.converter.getMappingContext().getRequiredPersistentEntity(target.getClass());
    if (entity.hasExplicitTimeToLiveProperty()) {

      RedisPersistentProperty ttlProperty = entity.getExplicitTimeToLiveProperty();
      if (ttlProperty == null) {
        return target;
      }

      TimeToLive ttl = ttlProperty.findAnnotation(TimeToLive.class);

      Long timeout = redisOperations.execute((RedisCallback<Long>) connection -> {

        if (ObjectUtils.nullSafeEquals(TimeUnit.SECONDS, ttl.unit())) {
          return connection.keyCommands().ttl(key);
        }

        return connection.keyCommands().pTtl(key, ttl.unit());
      });

      if (timeout != null || !ttlProperty.getType().isPrimitive()) {

        PersistentPropertyAccessor<T> propertyAccessor = entity.getPropertyAccessor(target);

        propertyAccessor.setProperty(ttlProperty, converter.getConversionService().convert(timeout, ttlProperty
            .getType()));

        target = propertyAccessor.getBean();
      }
    }

    return target;
  }

  /**
   * @param data must not be {@literal null}.
   * @return {@literal true} if {@link RedisData#getTimeToLive()} has a positive
   *         value.
   * @since 2.3.7
   */
  private boolean willExpire(RedisData data) {
    return data.getTimeToLive() != null && data.getTimeToLive() > 0;
  }

  /**
   * Constructs a Redis key by combining the keyspace and entity ID.
   * <p>
   * This method sanitizes the keyspace to ensure proper key format and
   * creates a string representation suitable for Redis key operations.
   * The resulting key follows the pattern: {@code keyspace:id}
   *
   * @param keyspace the Redis keyspace (namespace) for the entity
   * @param id       the entity identifier
   * @return the complete Redis key as a string
   */
  protected String getKey(String keyspace, Object id) {
    String sanitizedKeyspace = sanitizeKeyspace(keyspace);
    return String.format("%s:%s", sanitizedKeyspace, id);
  }

  /**
   * Creates a new {@link byte[] key} using the given {@link String keyspace} and {@link String id}.
   *
   * @param keyspace {@link String name} of the Redis {@literal keyspace}.
   * @param id       {@link String} identifying the key.
   * @return a {@link byte[]} constructed from the {@link String keyspace} and {@link String id}.
   */
  public byte[] createKey(String keyspace, String id) {
    // handle IdFilters
    var maybeIdentifierFilter = indexer.getIdentifierFilterFor(keyspace);
    if (maybeIdentifierFilter.isPresent()) {
      IdentifierFilter<String> filter = (IdentifierFilter<String>) maybeIdentifierFilter.get();
      id = filter.filter(id);
    }

    return toBytes(keyspace.endsWith(":") ? keyspace + id : keyspace + ":" + id);
  }

  /**
   * Resolves the dynamic keyspace from @IndexingOptions if present.
   * If the entity class has @IndexingOptions with a SpEL keyPrefix expression,
   * the expression is evaluated at runtime to determine the actual keyspace.
   *
   * @param entityClass     the entity class to check for @IndexingOptions
   * @param defaultKeyspace the default keyspace to use if no dynamic keyspace is configured
   * @return the resolved keyspace (either dynamic or default)
   */
  private String resolveDynamicKeyspace(Class<?> entityClass, String defaultKeyspace) {
    if (entityClass == null) {
      return defaultKeyspace;
    }
    // Use RediSearchIndexer to resolve dynamic keyspace from @IndexingOptions
    String dynamicKeyspace = indexer.getKeyspaceForEntityClass(entityClass);
    if (dynamicKeyspace != null && !dynamicKeyspace.isBlank()) {
      return dynamicKeyspace;
    }
    return defaultKeyspace;
  }

  /**
   * Container holding update information like fields to remove from the Redis
   * Hash.
   *
   * @author Christoph Strobl
   */
  static class RedisUpdateObject {
    private final byte[] targetKey;

    private final Set<byte[]> fieldsToRemove = new LinkedHashSet<>();

    RedisUpdateObject(byte[] targetKey) {
      this.targetKey = targetKey;
    }

    void addFieldToRemove(byte[] field) {
      fieldsToRemove.add(field);
    }
  }
}
