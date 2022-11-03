package com.redis.om.spring;

import static com.redis.om.spring.util.ObjectUtils.documentToObject;
import static com.redis.om.spring.util.ObjectUtils.getIdFieldForEntity;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.PartialUpdate;
import org.springframework.data.redis.core.PartialUpdate.PropertyUpdate;
import org.springframework.data.redis.core.PartialUpdate.UpdateCommand;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.convert.RedisConverter;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.redis.core.mapping.RedisPersistentProperty;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.convert.RedisOMCustomConversions;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;

import io.redisearch.Query;
import io.redisearch.SearchResult;

public class RedisEnhancedKeyValueAdapter extends RedisKeyValueAdapter {

  private RedisOperations<?, ?> redisOperations;
  private RedisConverter converter;
  private @Nullable String keyspaceNotificationsConfigParameter = null;
  private RedisModulesOperations<String> modulesOperations;
  private RediSearchIndexer indexer;

  /**
   * Creates new {@link RedisKeyValueAdapter} with default
   * {@link RedisMappingContext} and default {@link RedisCustomConversions}.
   *
   * @param redisOps           must not be {@literal null}.
   * @param rmo                must not be {@literal null}.
   * @param keyspaceToIndexMap must not be {@literal null}.
   */
  public RedisEnhancedKeyValueAdapter(RedisOperations<?, ?> redisOps, RedisModulesOperations<?> rmo,
      RediSearchIndexer keyspaceToIndexMap) {
    this(redisOps, rmo, new RedisMappingContext(), keyspaceToIndexMap);
  }

  /**
   * Creates new {@link RedisKeyValueAdapter} with default
   * {@link RedisCustomConversions}.
   *
   * @param redisOps           must not be {@literal null}.
   * @param rmo                must not be {@literal null}.
   * @param mappingContext     must not be {@literal null}.
   * @param keyspaceToIndexMap must not be {@literal null}.
   */
  public RedisEnhancedKeyValueAdapter(RedisOperations<?, ?> redisOps, RedisModulesOperations<?> rmo,
      RedisMappingContext mappingContext, RediSearchIndexer keyspaceToIndexMap) {
    this(redisOps, rmo, mappingContext, new RedisOMCustomConversions(), keyspaceToIndexMap);
  }

  /**
   * Creates new {@link RedisKeyValueAdapter}.
   *
   * @param redisOps           must not be {@literal null}.
   * @param rmo                must not be {@literal null}.
   * @param mappingContext     must not be {@literal null}.
   * @param customConversions  can be {@literal null}.
   * @param keyspaceToIndexMap must not be {@literal null}.
   */
  @SuppressWarnings("unchecked")
  public RedisEnhancedKeyValueAdapter(RedisOperations<?, ?> redisOps, RedisModulesOperations<?> rmo,
      RedisMappingContext mappingContext,
      @Nullable org.springframework.data.convert.CustomConversions customConversions,
      RediSearchIndexer keyspaceToIndexMap) {
    super(redisOps, mappingContext, customConversions);

    Assert.notNull(redisOps, "RedisOperations must not be null!");
    Assert.notNull(mappingContext, "RedisMappingContext must not be null!");

    MappingRedisOMConverter mappingConverter = new MappingRedisOMConverter(mappingContext,
        new ReferenceResolverImpl(redisOps));
    mappingConverter
        .setCustomConversions(customConversions == null ? new RedisOMCustomConversions() : customConversions);
    mappingConverter.afterPropertiesSet();

    this.converter = mappingConverter;
    this.redisOperations = redisOps;
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.indexer = keyspaceToIndexMap;
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
    if (item instanceof RedisData) {
      rdo = (RedisData) item;
    } else {
      byte[] redisKey = createKey(keyspace, converter.getConversionService().convert(id, String.class));
      processAuditAnnotations(redisKey, item);

      rdo = new RedisData();
      converter.write(item, rdo);
    }

    if (rdo.getId() == null) {
      rdo.setId(converter.getConversionService().convert(id, String.class));
    }

    byte[] objectKey = createKey(rdo.getKeyspace(), rdo.getId());
    redisOperations.execute((RedisCallback<Boolean>) connection -> connection.del(objectKey) == 0);

    redisOperations.executePipelined((RedisCallback<Object>) connection -> {
      Map<byte[], byte[]> rawMap = rdo.getBucket().rawMap();
      connection.hMSet(objectKey, rawMap);

      if (expires(rdo)) {
        connection.expire(objectKey, rdo.getTimeToLive());
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

    String stringId = asString(id);
    String stringKeyspace = asString(keyspace);

    byte[] binId = createKey(stringKeyspace, stringId);

    Map<byte[], byte[]> raw = redisOperations
        .execute((RedisCallback<Map<byte[], byte[]>>) connection -> connection.hGetAll(binId));

    if (CollectionUtils.isEmpty(raw)) {
      return null;
    }

    RedisData data = new RedisData(raw);
    data.setId(stringId);
    data.setKeyspace(stringKeyspace);

    return readBackTimeToLiveIfSet(binId, converter.read(type, data));
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
    T o = get(id, keyspace, type);

    if (o != null) {

      byte[] keyToDelete = createKey(asString(keyspace), asString(id));

      redisOperations.execute((RedisCallback<Void>) connection -> {
        connection.del(keyToDelete);
        return null;
      });
    }

    return o;
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
      searchOps.dropIndex();
      indexer.createIndexFor(type);
    }
  }

  public <T> List<String> getAllIds(String keyspace, Class<T> type) {
    Optional<String> maybeSearchIndex = indexer.getIndexName(keyspace);
    List<String> keys = List.of();
    if (maybeSearchIndex.isPresent()) {
      SearchOperations<String> searchOps = modulesOperations.opsForSearch(maybeSearchIndex.get());
      Optional<Field> maybeIdField = com.redis.om.spring.util.ObjectUtils.getIdFieldForEntityClass(type);
      String idField = maybeIdField.isPresent() ? maybeIdField.get().getName() : "id";

      Query query = new Query("*");
      query.returnFields(idField);
      SearchResult searchResult = searchOps.search(query);

      keys = searchResult.docs.stream()
          .map(d -> documentToObject(d, type, (MappingRedisOMConverter) converter)) //
          .map(e -> getIdFieldForEntity(maybeIdField.get(), e)) //
          .map(Object::toString)
          .collect(Collectors.toList());
    }

    return keys;
  }

  /**
   * Get all elements for given keyspace.
   *
   * @param keyspace the keyspace to fetch entities from.
   * @param type     the desired target type.
   * @param offset   index value to start reading.
   * @param rows     maximum number or entities to return.
   * @param <T>      the target type
   * @return never {@literal null}.
   * @since 2.5
   */
  @SuppressWarnings("unchecked")
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

      result = (List<T>) searchResult.docs.stream() //
          .map(d -> documentToObject(d, type, (MappingRedisOMConverter)converter)) //
          .collect(Collectors.toList());
    }

    return result;
  }

  @Override
  public void update(PartialUpdate<?> update) {

    RedisPersistentEntity<?> entity = this.converter.getMappingContext()
        .getRequiredPersistentEntity(update.getTarget());

    String keyspace = entity.getKeySpace();
    Object id = update.getId();

    byte[] redisKey = createKey(keyspace, converter.getConversionService().convert(id, String.class));

    RedisData rdo = new RedisData();
    this.converter.write(update, rdo);

    redisOperations.execute((RedisCallback<Void>) connection -> {

      RedisUpdateObject redisUpdateObject = new RedisUpdateObject(redisKey);

      for (PropertyUpdate pUpdate : update.getPropertyUpdates()) {

        String propertyPath = pUpdate.getPropertyPath();

        if (UpdateCommand.DEL.equals(pUpdate.getCmd()) || pUpdate.getValue() instanceof Collection
            || pUpdate.getValue() instanceof Map
            || (pUpdate.getValue() != null && pUpdate.getValue().getClass().isArray()) || (pUpdate.getValue() != null
                && !converter.getConversionService().canConvert(pUpdate.getValue().getClass(), byte[].class))) {

          redisUpdateObject = fetchDeletePathsFromHash(redisUpdateObject, propertyPath, connection);
        }
      }

      if (!redisUpdateObject.fieldsToRemove.isEmpty()) {
        connection.hDel(redisKey,
            redisUpdateObject.fieldsToRemove.toArray(new byte[redisUpdateObject.fieldsToRemove.size()][]));
      }

      if (!rdo.getBucket().isEmpty()) {
        if (rdo.getBucket().size() > 1
            || (rdo.getBucket().size() == 1 && !rdo.getBucket().asMap().containsKey("_class"))) {
          connection.hMSet(redisKey, rdo.getBucket().rawMap());
        }
      }

      if (update.isRefreshTtl()) {

        if (expires(rdo)) {
          connection.expire(redisKey, rdo.getTimeToLive());
        } else {
          connection.persist(redisKey);
        }
      }

      return null;
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.data.keyvalue.core.KeyValueAdapter#count(java.lang.
   * String)
   */
  @Override
  public long count(String keyspace) {
    Long count = 0L;
    Optional<String> maybeIndexName = indexer.getIndexName(keyspace);
    if (maybeIndexName.isPresent()) {
      SearchOperations<String> search = modulesOperations.opsForSearch(maybeIndexName.get());
      // FT.SEARCH index * LIMIT 0 0
      Query query = new Query("*");
      query.limit(0, 0);
      
      SearchResult result = search.search(query);
      
      count = result.totalResults;
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
        .execute((RedisCallback<Boolean>) connection -> connection.exists(toBytes(getKey(keyspace, id))));

    return exists != null ? exists : false;
  }

  protected String getKey(String keyspace, Object id) {
    return String.format("%s:%s", keyspace, id);
  }

  private RedisUpdateObject fetchDeletePathsFromHash(RedisUpdateObject redisUpdateObject, String path,
      RedisConnection connection) {

    redisUpdateObject.addFieldToRemove(toBytes(path));
    byte[] value = connection.hGet(redisUpdateObject.targetKey, toBytes(path));

    if (value != null && value.length > 0) {
      return redisUpdateObject;
    }

    Set<byte[]> existingFields = connection.hKeys(redisUpdateObject.targetKey);

    for (byte[] field : existingFields) {

      if (asString(field).startsWith(path + ".")) {
        redisUpdateObject.addFieldToRemove(field);
        value = connection.hGet(redisUpdateObject.targetKey, toBytes(field));
      }
    }

    return redisUpdateObject;
  }

  private String asString(Object value) {
    return value instanceof String ? (String) value
        : getConverter().getConversionService().convert(value, String.class);
  }

  /**
   * Read back and set {@link TimeToLive} for the property.
   *
   * @param key
   * @param target
   * @return
   */
  @Nullable
  private <T> T readBackTimeToLiveIfSet(@Nullable byte[] key, @Nullable T target) {

    if (target == null || key == null) {
      return target;
    }

    RedisPersistentEntity<?> entity = this.converter.getMappingContext().getRequiredPersistentEntity(target.getClass());
    if (entity.hasExplictTimeToLiveProperty()) {

      RedisPersistentProperty ttlProperty = entity.getExplicitTimeToLiveProperty();
      if (ttlProperty == null) {
        return target;
      }

      TimeToLive ttl = ttlProperty.findAnnotation(TimeToLive.class);

      Long timeout = redisOperations.execute((RedisCallback<Long>) connection -> {

        if (ObjectUtils.nullSafeEquals(TimeUnit.SECONDS, ttl.unit())) {
          return connection.ttl(key);
        }

        return connection.pTtl(key, ttl.unit());
      });

      if (timeout != null || !ttlProperty.getType().isPrimitive()) {

        PersistentPropertyAccessor<T> propertyAccessor = entity.getPropertyAccessor(target);

        propertyAccessor.setProperty(ttlProperty,
            converter.getConversionService().convert(timeout, ttlProperty.getType()));

        target = propertyAccessor.getBean();
      }
    }

    return target;
  }

  /**
   * @return {@literal true} if {@link RedisData#getTimeToLive()} has a positive
   *         value.
   *
   * @param data must not be {@literal null}.
   * @since 2.3.7
   */
  private boolean expires(RedisData data) {
    return data.getTimeToLive() != null && data.getTimeToLive() > 0;
  }

  private void processAuditAnnotations(byte[] redisKey, Object item) {
    boolean isNew = (boolean) redisOperations
        .execute((RedisCallback<Object>) connection -> !connection.exists(redisKey));

    var auditClass = isNew ? CreatedDate.class : LastModifiedDate.class;

    List<Field> fields = com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(item.getClass(), auditClass);
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
