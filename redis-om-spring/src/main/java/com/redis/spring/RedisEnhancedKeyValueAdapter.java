package com.redis.spring;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.keyvalue.core.QueryEngine;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.PartialUpdate;
import org.springframework.data.redis.core.PartialUpdate.PropertyUpdate;
import org.springframework.data.redis.core.PartialUpdate.UpdateCommand;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.convert.GeoIndexedPropertyValue;
import org.springframework.data.redis.core.convert.MappingRedisConverter;
import org.springframework.data.redis.core.convert.MappingRedisConverter.BinaryKeyspaceIdentifier;
import org.springframework.data.redis.core.convert.MappingRedisConverter.KeyspaceIdentifier;
import org.springframework.data.redis.core.convert.PathIndexResolver;
import org.springframework.data.redis.core.convert.RedisConverter;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.redis.core.mapping.RedisPersistentProperty;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.util.ByteUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

public class RedisEnhancedKeyValueAdapter extends RedisKeyValueAdapter {

  /**
   * Time To Live in seconds that phantom keys should live longer than the actual
   * key.
   */
  private static final int PHANTOM_KEY_TTL = 300;

  private RedisOperations<?, ?> redisOperations;
  private RedisConverter converter;
  private @Nullable RedisMessageListenerContainer messageListenerContainer;
  private final AtomicReference<KeyExpirationEventMessageListener> expirationListener = new AtomicReference<>(null);
  private @Nullable ApplicationEventPublisher eventPublisher;

  private EnableKeyspaceEvents enableKeyspaceEvents = EnableKeyspaceEvents.OFF;
  private @Nullable String keyspaceNotificationsConfigParameter = null;
  private ShadowCopy shadowCopy = ShadowCopy.DEFAULT;
  
  private QueryEngine<? extends KeyValueAdapter, ?, ?> queryEngine;

  /**
   * Creates new {@link RedisKeyValueAdapter} with default
   * {@link RedisMappingContext} and default {@link RedisCustomConversions}.
   *
   * @param redisOps must not be {@literal null}.
   */
  public RedisEnhancedKeyValueAdapter(RedisOperations<?, ?> redisOps) {
    this(redisOps, new RedisMappingContext());
  }

  /**
   * Creates new {@link RedisKeyValueAdapter} with default
   * {@link RedisCustomConversions}.
   *
   * @param redisOps       must not be {@literal null}.
   * @param mappingContext must not be {@literal null}.
   */
  public RedisEnhancedKeyValueAdapter(RedisOperations<?, ?> redisOps, RedisMappingContext mappingContext) {
    this(redisOps, mappingContext, new RedisCustomConversions());
  }

  /**
   * Creates new {@link RedisKeyValueAdapter}.
   *
   * @param redisOps          must not be {@literal null}.
   * @param mappingContext    must not be {@literal null}.
   * @param customConversions can be {@literal null}.
   * @since 2.0
   */
  public RedisEnhancedKeyValueAdapter(RedisOperations<?, ?> redisOps, RedisMappingContext mappingContext,
      @Nullable org.springframework.data.convert.CustomConversions customConversions) {
    super(redisOps, mappingContext, customConversions);
    queryEngine = new EnhancedRedisQueryEngine();

    Assert.notNull(redisOps, "RedisOperations must not be null!");
    Assert.notNull(mappingContext, "RedisMappingContext must not be null!");

    MappingRedisConverter mappingConverter = new MappingRedisConverter(mappingContext,
        new PathIndexResolver(mappingContext), new ReferenceResolverImpl(redisOps));
    mappingConverter.setCustomConversions(customConversions == null ? new RedisCustomConversions() : customConversions);
    mappingConverter.afterPropertiesSet();

    this.converter = mappingConverter;
    this.redisOperations = redisOps;
    initMessageListenerContainer();
  }

  /**
   * Creates new {@link RedisKeyValueAdapter} with specific
   * {@link RedisConverter}.
   *
   * @param redisOps       must not be {@literal null}.
   * @param redisConverter must not be {@literal null}.
   */
  public RedisEnhancedKeyValueAdapter(RedisOperations<?, ?> redisOps, RedisConverter redisConverter) {
    queryEngine = new EnhancedRedisQueryEngine();

    Assert.notNull(redisOps, "RedisOperations must not be null!");

    this.converter = redisConverter;
    this.redisOperations = redisOps;
    initMessageListenerContainer();
  }

  /**
   * Default constructor.
   */
  protected RedisEnhancedKeyValueAdapter() {}
  
  /**
   * Get the {@link QueryEngine} used.
   *
   * @return
   */
  public QueryEngine<? extends KeyValueAdapter, ?, ?> getQueryEngine() {
    return queryEngine;
  }

  /* (non-Javadoc)
   * 
   * @see
   * org.springframework.data.keyvalue.core.KeyValueAdapter#put(java.lang.Object,
   * java.lang.Object, java.lang.String) */
  @Override
  public Object put(Object id, Object item, String keyspace) {

    RedisData rdo = item instanceof RedisData ? (RedisData) item : new RedisData();
    if (!(item instanceof RedisData)) {
      converter.write(item, rdo);
    }

    if (ObjectUtils.nullSafeEquals(EnableKeyspaceEvents.ON_DEMAND, enableKeyspaceEvents)
        && this.expirationListener.get() == null) {

      if (rdo.getTimeToLive() != null && rdo.getTimeToLive() > 0) {
        initKeyExpirationListener();
      }
    }

    if (rdo.getId() == null) {
      rdo.setId(converter.getConversionService().convert(id, String.class));
    }

    redisOperations.execute((RedisCallback<Object>) connection -> {

      byte[] key = toBytes(rdo.getId());
      byte[] objectKey = createKey(rdo.getKeyspace(), rdo.getId());

      boolean isNew = connection.del(objectKey) == 0;

      connection.hMSet(objectKey, rdo.getBucket().rawMap());

      if (isNew) {
        connection.sAdd(toBytes(rdo.getKeyspace()), key);
      }

      if (expires(rdo)) {
        connection.expire(objectKey, rdo.getTimeToLive());
      }

      if (keepShadowCopy()) { // add phantom key so values can be restored

        byte[] phantomKey = ByteUtils.concat(objectKey, BinaryKeyspaceIdentifier.PHANTOM_SUFFIX);

        if (expires(rdo)) {

          connection.del(phantomKey);
          connection.hMSet(phantomKey, rdo.getBucket().rawMap());
          connection.expire(phantomKey, rdo.getTimeToLive() + PHANTOM_KEY_TTL);
        } else if (!isNew) {
          connection.del(phantomKey);
        }
      }

//      IndexWriter indexWriter = new IndexWriter(connection, converter);
//      if (isNew) {
//        indexWriter.createIndexes(key, rdo.getIndexedData());
//      } else {
//        indexWriter.deleteAndUpdateIndexes(key, rdo.getIndexedData());
//      }
      return null;
    });

    return item;
  }

  /* (non-Javadoc)
   * 
   * @see
   * org.springframework.data.keyvalue.core.KeyValueAdapter#deleteAllOf(java.lang.
   * String) */
  @Override
  public void deleteAllOf(String keyspace) {

    redisOperations.execute((RedisCallback<Void>) connection -> {

      connection.del(toBytes(keyspace));
//      new IndexWriter(connection, converter).removeAllIndexes(asString(keyspace));
      return null;
    });
  }

  public void update(PartialUpdate<?> update) {

    RedisPersistentEntity<?> entity = this.converter.getMappingContext()
        .getRequiredPersistentEntity(update.getTarget());

    String keyspace = entity.getKeySpace();
    Object id = update.getId();

    byte[] redisKey = createKey(keyspace, converter.getConversionService().convert(id, String.class));

    RedisData rdo = new RedisData();
    this.converter.write(update, rdo);

    redisOperations.execute((RedisCallback<Void>) connection -> {

      RedisUpdateObject redisUpdateObject = new RedisUpdateObject(redisKey, keyspace, id);

      for (PropertyUpdate pUpdate : update.getPropertyUpdates()) {

        String propertyPath = pUpdate.getPropertyPath();

        if (UpdateCommand.DEL.equals(pUpdate.getCmd()) || pUpdate.getValue() instanceof Collection
            || pUpdate.getValue() instanceof Map
            || (pUpdate.getValue() != null && pUpdate.getValue().getClass().isArray()) || (pUpdate.getValue() != null
                && !converter.getConversionService().canConvert(pUpdate.getValue().getClass(), byte[].class))) {

          redisUpdateObject = fetchDeletePathsFromHashAndUpdateIndex(redisUpdateObject, propertyPath, connection);
        }
      }

      if (!redisUpdateObject.fieldsToRemove.isEmpty()) {
        connection.hDel(redisKey,
            redisUpdateObject.fieldsToRemove.toArray(new byte[redisUpdateObject.fieldsToRemove.size()][]));
      }

//      for (Index index : redisUpdateObject.indexesToUpdate) {
//
//        if (ObjectUtils.nullSafeEquals(DataType.ZSET, index.type)) {
//          connection.zRem(index.key, toBytes(redisUpdateObject.targetId));
//        } else {
//          connection.sRem(index.key, toBytes(redisUpdateObject.targetId));
//        }
//      }

      if (!rdo.getBucket().isEmpty()) {
        if (rdo.getBucket().size() > 1
            || (rdo.getBucket().size() == 1 && !rdo.getBucket().asMap().containsKey("_class"))) {
          connection.hMSet(redisKey, rdo.getBucket().rawMap());
        }
      }

      if (update.isRefreshTtl()) {

        if (expires(rdo)) {

          connection.expire(redisKey, rdo.getTimeToLive());

          if (keepShadowCopy()) { // add phantom key so values can be restored

            byte[] phantomKey = ByteUtils.concat(redisKey, BinaryKeyspaceIdentifier.PHANTOM_SUFFIX);
            connection.hMSet(phantomKey, rdo.getBucket().rawMap());
            connection.expire(phantomKey, rdo.getTimeToLive() + PHANTOM_KEY_TTL);
          }

        } else {

          connection.persist(redisKey);

          if (keepShadowCopy()) {
            connection.del(ByteUtils.concat(redisKey, BinaryKeyspaceIdentifier.PHANTOM_SUFFIX));
          }
        }
      }

//      new IndexWriter(connection, converter).updateIndexes(toBytes(id), rdo.getIndexedData());
      return null;
    });
  }

  private RedisUpdateObject fetchDeletePathsFromHashAndUpdateIndex(RedisUpdateObject redisUpdateObject, String path,
      RedisConnection connection) {

    redisUpdateObject.addFieldToRemove(toBytes(path));
    byte[] value = connection.hGet(redisUpdateObject.targetKey, toBytes(path));

    if (value != null && value.length > 0) {

      byte[] existingValueIndexKey = ByteUtils.concatAll(toBytes(redisUpdateObject.keyspace), toBytes(":" + path),
          toBytes(":"), value);

      if (connection.exists(existingValueIndexKey)) {
        redisUpdateObject.addIndexToUpdate(new RedisUpdateObject.Index(existingValueIndexKey, DataType.SET));
      }

      return redisUpdateObject;
    }

    Set<byte[]> existingFields = connection.hKeys(redisUpdateObject.targetKey);

    for (byte[] field : existingFields) {

      if (asString(field).startsWith(path + ".")) {

        redisUpdateObject.addFieldToRemove(field);
        value = connection.hGet(redisUpdateObject.targetKey, toBytes(field));

        if (value != null) {

          byte[] existingValueIndexKey = ByteUtils.concatAll(toBytes(redisUpdateObject.keyspace), toBytes(":"), field,
              toBytes(":"), value);

          if (connection.exists(existingValueIndexKey)) {
            redisUpdateObject.addIndexToUpdate(new RedisUpdateObject.Index(existingValueIndexKey, DataType.SET));
          }
        }
      }
    }

    String pathToUse = GeoIndexedPropertyValue.geoIndexName(path);
    byte[] existingGeoIndexKey = ByteUtils.concatAll(toBytes(redisUpdateObject.keyspace), toBytes(":"),
        toBytes(pathToUse));

    if (connection.zRank(existingGeoIndexKey, toBytes(redisUpdateObject.targetId)) != null) {
      redisUpdateObject.addIndexToUpdate(new RedisUpdateObject.Index(existingGeoIndexKey, DataType.ZSET));
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

  private void initMessageListenerContainer() {

    this.messageListenerContainer = new RedisMessageListenerContainer();
    this.messageListenerContainer.setConnectionFactory(((RedisTemplate<?, ?>) redisOperations).getConnectionFactory());
    this.messageListenerContainer.afterPropertiesSet();
    this.messageListenerContainer.start();
  }

  private void initKeyExpirationListener() {

    if (this.expirationListener.get() == null) {

      MappingExpirationListener listener = new MappingExpirationListener(this.messageListenerContainer, this.redisOperations,
          this.converter);
      listener.setKeyspaceNotificationsConfigParameter(keyspaceNotificationsConfigParameter);

      if (this.eventPublisher != null) {
        listener.setApplicationEventPublisher(this.eventPublisher);
      }

      if (this.expirationListener.compareAndSet(null, listener)) {
        listener.init();
      }
    }
  }

  /**
   * {@link MessageListener} implementation used to capture Redis keyspace
   * notifications. Tries to read a previously created phantom key
   * {@code keyspace:id:phantom} to provide the expired object as part of the
   * published {@link RedisKeyExpiredEvent}.
   *
   * @author Christoph Strobl
   * @since 1.7
   */
  static class MappingExpirationListener extends KeyExpirationEventMessageListener {

    private final RedisOperations<?, ?> ops;
    private final RedisConverter converter;

    /**
     * Creates new {@link MappingExpirationListener}.
     *
     * @param listenerContainer
     * @param ops
     * @param converter
     */
    MappingExpirationListener(RedisMessageListenerContainer listenerContainer, RedisOperations<?, ?> ops,
        RedisConverter converter) {

      super(listenerContainer);
      this.ops = ops;
      this.converter = converter;
    }

    /* (non-Javadoc)
     * 
     * @see org.springframework.data.redis.listener.KeyspaceEventMessageListener#
     * onMessage(org.springframework.data.redis.connection.Message, byte[]) */
    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {

      if (!isKeyExpirationMessage(message)) {
        return;
      }

      byte[] key = message.getBody();

      byte[] phantomKey = ByteUtils.concat(key,
          converter.getConversionService().convert(KeyspaceIdentifier.PHANTOM_SUFFIX, byte[].class));

      Map<byte[], byte[]> hash = ops.execute((RedisCallback<Map<byte[], byte[]>>) connection -> {

        Map<byte[], byte[]> hash1 = connection.hGetAll(phantomKey);

        if (!CollectionUtils.isEmpty(hash1)) {
          connection.del(phantomKey);
        }

        return hash1;
      });

      Object value = CollectionUtils.isEmpty(hash) ? null : converter.read(Object.class, new RedisData(hash));

      byte[] channelAsBytes = message.getChannel();
      String channel = !ObjectUtils.isEmpty(channelAsBytes)
          ? converter.getConversionService().convert(channelAsBytes, String.class)
          : null;

      @SuppressWarnings("rawtypes")
      RedisKeyExpiredEvent event = new RedisKeyExpiredEvent(channel, key, value);

      ops.execute((RedisCallback<Void>) connection -> {

        connection.sRem(converter.getConversionService().convert(event.getKeyspace(), byte[].class), event.getId());
//        new IndexWriter(connection, converter).removeKeyFromIndexes(event.getKeyspace(), event.getId());
        return null;
      });

      publishEvent(event);
    }

    private boolean isKeyExpirationMessage(Message message) {
      return BinaryKeyspaceIdentifier.isValid(message.getBody());
    }
  }

  private boolean keepShadowCopy() {

    switch (shadowCopy) {
      case OFF:
        return false;
      case ON:
        return true;
      default:
        return this.expirationListener.get() != null;
    }
  }

  /**
   * Container holding update information like fields to remove from the Redis
   * Hash.
   *
   * @author Christoph Strobl
   */
  static class RedisUpdateObject {

    private final String keyspace;
    private final Object targetId;
    private final byte[] targetKey;

    private final Set<byte[]> fieldsToRemove = new LinkedHashSet<>();
    private final Set<Index> indexesToUpdate = new LinkedHashSet<>();

    RedisUpdateObject(byte[] targetKey, String keyspace, Object targetId) {

      this.targetKey = targetKey;
      this.keyspace = keyspace;
      this.targetId = targetId;
    }

    void addFieldToRemove(byte[] field) {
      fieldsToRemove.add(field);
    }

    void addIndexToUpdate(Index index) {
      indexesToUpdate.add(index);
    }

    static class Index {
      final DataType type;
      final byte[] key;

      public Index(byte[] key, DataType type) {
        this.key = key;
        this.type = type;
      }

    }
  }
}
