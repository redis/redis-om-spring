package com.redis.om.cache;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.NullValue;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.redis.lettucemod.RedisModulesUtils;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.search.CreateOptions;
import com.redis.lettucemod.search.CreateOptions.DataType;
import com.redis.lettucemod.search.Field;
import com.redis.lettucemod.search.IndexInfo;

import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.TrackingArgs;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.StringCodec;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

/**
 * {@link AbstractValueAdaptingCache Cache} implementation using Redis as the underlying store for cache data.
 * <p>
 * Use {@link RedisCacheManager} to create {@link RedisCache} instances.
 */
public class RedisCache extends AbstractValueAdaptingCache implements AutoCloseable {

  private static final String DESCRIPTION_GETS = "The number of times cache lookup methods have returned a cached (hit) or uncached (miss) value.";

  private static final String DESCRIPTION_PUTS = "The number of entries added to the cache.";

  private static final String DESCRIPTION_EVICTIONS = "The number of times the cache was evicted";

  static final String CACHE_RETRIEVAL_UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE = "The Redis driver configured with RedisCache through RedisCacheWriter does not support CompletableFuture-based retrieval";

  private final String name;

  private final AbstractRedisClient redisClient;

  private final StatefulRedisModulesConnection<byte[], byte[]> connection;

  private final CacheAccessor accessor;

  private final RedisCacheConfiguration configuration;

  private final Counter hits;

  private final Counter misses;

  private final Counter puts;

  private final Counter evictions;

  private final Timer getLatency;

  private final Timer putLatency;

  private final Timer evictionLatency;

  /**
   * Create a new {@link RedisCache} with the given {@link String name} and {@link RedisCacheConfiguration}, using the
   * {@link CacheAccessor} to execute Redis commands supporting the cache operations.
   *
   * @param name          {@link String name} for this {@link Cache}; must not be {@literal null}.
   * @param client        Default {@link AbstractRedisClient} to use if none specified in given RedisCacheConfiguration.
   * @param configuration {@link RedisCacheConfiguration} applied to this {@link RedisCache} on creation; must not be
   *                      {@literal null}.
   * @throws IllegalArgumentException if either the given {@link CacheAccessor} or {@link RedisCacheConfiguration} are
   *                                  {@literal null} or the given {@link String} name for this {@link RedisCache} is
   *                                  {@literal null}.
   */
  public RedisCache(String name, AbstractRedisClient client, RedisCacheConfiguration configuration) {
    super(false);
    Assert.notNull(name, "Name must not be null");
    this.name = name;
    this.redisClient = configuration.getClient() == null ? client : configuration.getClient();
    this.connection = RedisModulesUtils.connection(redisClient, ByteArrayCodec.INSTANCE);
    this.configuration = configuration;
    this.accessor = accessor();
    this.hits = Counter.builder("cache.gets").tags("name", name).tag("result", "hit").description(DESCRIPTION_GETS)
        .register(configuration.getMeterRegistry());
    this.misses = Counter.builder("cache.gets").tag("name", name).tag("result", "miss").description(DESCRIPTION_GETS)
        .register(configuration.getMeterRegistry());
    this.puts = Counter.builder("cache.puts").tag("name", name).description(DESCRIPTION_PUTS).register(configuration
        .getMeterRegistry());
    this.evictions = Counter.builder("cache.evictions").tag("name", name).description(DESCRIPTION_EVICTIONS).register(
        configuration.getMeterRegistry());
    this.getLatency = Timer.builder("cache.gets.latency").tag("name", name).description("Cache gets").register(
        configuration.getMeterRegistry());
    this.putLatency = Timer.builder("cache.puts.latency").tag("name", name).description("Cache puts").register(
        configuration.getMeterRegistry());
    this.evictionLatency = Timer.builder("cache.evictions.latency").tag("name", name).description("Cache evictions")
        .register(configuration.getMeterRegistry());
    if (configuration.isIndexEnabled()) {
      createIndex();
    }
  }

  @SuppressWarnings(
    "unchecked"
  )
  private void createIndex() {
    CreateOptions.Builder<String, String> createOptions = CreateOptions.builder();
    createOptions.on(indexType());
    createOptions.prefix(getName() + KeyFunction.SEPARATOR);
    createOptions.noFields(); // Disable storing attribute bits for each term. It saves memory, but it does not allow
    // filtering by specific attributes.
    try (StatefulRedisModulesConnection<String, String> connection = connection()) {
      String indexName = indexName();
      try {
        connection.sync().ftDropindex(indexName);
      } catch (RedisCommandExecutionException e) {
        // ignore as index might not exist
      }
      connection.sync().ftCreate(indexName, createOptions.build(), indexField());
    }
  }

  private DataType indexType() {
    switch (configuration.getRedisType()) {
      case HASH:
        return DataType.HASH;
      case JSON:
        return DataType.JSON;
      default:
        throw new IllegalArgumentException(String.format("Redis type %s not indexable", configuration.getRedisType()));
    }
  }

  private StatefulRedisModulesConnection<String, String> connection() {
    return RedisModulesUtils.connection(redisClient);
  }

  /**
   * Returns the number of documents in the index if enabled.
   *
   * @return the number of documents in the index or -1 if cache is not indexed
   */
  public long getCount() {
    if (configuration.isIndexEnabled()) {
      try (StatefulRedisModulesConnection<String, String> connection = connection()) {
        IndexInfo cacheInfo = RedisModulesUtils.indexInfo(connection.sync().ftInfo(indexName()));
        Double numDocs = cacheInfo.getNumDocs();
        if (numDocs != null) {
          return numDocs.longValue();
        }
      }
    }
    return -1;
  }

  private String indexName() {
    if (StringUtils.hasLength(configuration.getIndexName())) {
      return configuration.getIndexName();
    }
    return name + "Idx";
  }

  private Field<String> indexField() {
    if (configuration.getRedisType() == RedisType.JSON) {
      return Field.tag("$._class").as("_class").build();
    }
    return Field.tag("_class").build();
  }

  @Override
  public void close() {
    connection.close();
  }

  @SuppressWarnings(
    "unchecked"
  )
  private CacheAccessor accessor() {
    CacheAccessor redisCacheAccessor = redisCacheAccessor();
    if (configuration.getLocalCache().isPresent()) {
      connection.sync().clientTracking(TrackingArgs.Builder.enabled());
      LocalCacheAccessor localCacheAccessor = new LocalCacheAccessor(configuration.getLocalCache().get(),
          redisCacheAccessor, name, configuration.getMeterRegistry());
      connection.addListener(msg -> {
        if (msg.getType().equals("invalidate")) {
          List<Object> content = msg.getContent(StringCodec.UTF8::decodeKey);
          List<String> keys = (List<String>) content.get(1);
          keys.forEach(localCacheAccessor::evictLocal);
        }
      });
      return localCacheAccessor;
    }
    return redisCacheAccessor;
  }

  private CacheAccessor redisCacheAccessor() {
    switch (configuration.getRedisType()) {
      case JSON:
        return new RedisJsonCacheAccessor(connection, configuration.getJsonMapper());
      case STRING:
        return new RedisStringCacheAccessor(connection, configuration.getStringMapper());
      default:
        return new RedisHashCacheAccessor(connection, configuration.getHashMapper());
    }
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public CacheAccessor getNativeCache() {
    return accessor;
  }

  @Override
  @SuppressWarnings(
    "unchecked"
  )
  public <V> V get(Object key, Callable<V> valueLoader) {
    ValueWrapper result = get(key);
    if (result == null) {
      return loadCacheValue(key, valueLoader);
    }
    return (V) result.get();
  }

  /**
   * Loads the {@link Object} using the given {@link Callable valueLoader} and {@link #put(Object, Object) puts} the
   * {@link Object loaded value} in the cache.
   *
   * @param <V>         {@link Class type} of the loaded {@link Object cache value}.
   * @param key         {@link Object key} mapped to the loaded {@link Object cache value}.
   * @param valueLoader {@link Callable} object used to load the {@link Object value} for the given {@link Object key}.
   * @return the loaded {@link Object value}.
   */
  protected <V> V loadCacheValue(Object key, Callable<V> valueLoader) {
    V value;
    try {
      value = valueLoader.call();
    } catch (Exception ex) {
      throw new ValueRetrievalException(key, valueLoader, ex);
    }
    put(key, value);
    return value;
  }

  @Override
  protected Object lookup(Object key) {
    return getLatency.record(() -> {
      Object result = accessor.get(cacheKey(key), lookupTtl(key));
      if (result == null) {
        misses.increment();
      } else {
        hits.increment();
      }
      return result;
    });
  }

  private Duration lookupTtl(Object key) {
    if (configuration.isExpireOnGet()) {
      return ttl(key);
    }
    return TtlFunction.NO_EXPIRATION;
  }

  /**
   * Returns the configuration used by this cache instance.
   *
   * @return the RedisCacheConfiguration instance that defines the behavior of this cache
   */
  public RedisCacheConfiguration getConfiguration() {
    return configuration;
  }

  private Duration ttl(Object key) {
    return ttl(key, null);
  }

  private Duration ttl(Object key, @Nullable Object value) {
    return configuration.getTtlFunction().getTtl(key, value);
  }

  @Override
  public void put(Object key, @Nullable Object value) {
    putLatency.record(() -> {
      Object cacheValue = processAndCheckValue(value);
      String cacheKey = cacheKey(key);
      accessor.put(cacheKey, cacheValue, ttl(key, value));
      puts.increment();
    });
  }

  @Override
  public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
    return putLatency.record(() -> {
      Object cacheValue = preProcessCacheValue(value);
      if (nullCacheValueIsNotAllowed(cacheValue)) {
        return get(key);
      }
      Duration ttl = ttl(key, value);
      String cacheKey = cacheKey(key);
      Object result = accessor.putIfAbsent(cacheKey, cacheValue, ttl);
      if (result == null) {
        puts.increment();
        return null;
      }
      return new SimpleValueWrapper(fromStoreValue(result));
    });
  }

  @Override
  public void clear() {
    clear("*");
  }

  /**
   * Clear keys that match the given {@link String keyPattern}.
   * <p>
   * Useful when cache keys are formatted in a style where Redis patterns can be used for matching these.
   *
   * @param keyPattern {@link String pattern} used to match Redis keys to clear.
   */
  public void clear(String keyPattern) {
    long count = accessor.clean(cacheKey(keyPattern));
    evictions.increment(count);
  }

  @Override
  public void evict(Object key) {
    evictionLatency.record(() -> {
      accessor.remove(cacheKey(key));
      evictions.increment();
    });
  }

  @Override
  public CompletableFuture<ValueWrapper> retrieve(Object key) {
    throw new UnsupportedOperationException(CACHE_RETRIEVAL_UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
  }

  @Override
  public <V> CompletableFuture<V> retrieve(Object key, Supplier<CompletableFuture<V>> valueLoader) {
    throw new UnsupportedOperationException(CACHE_RETRIEVAL_UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
  }

  private Object processAndCheckValue(@Nullable Object value) {
    Object cacheValue = preProcessCacheValue(value);
    if (nullCacheValueIsNotAllowed(cacheValue)) {
      String message = String.format(
          "Cache '%s' does not allow 'null' values; Avoid storing null" + " via '@Cacheable(unless=\"#result == null\")' or configure RedisCache to allow 'null'" + " via RedisCacheConfiguration",
          getName());
      throw new IllegalArgumentException(message);
    }
    return cacheValue;
  }

  /**
   * Customization hook called before serializing object.
   *
   * @param value can be {@literal null}.
   * @return preprocessed value. Can be {@literal null}.
   */
  @Nullable
  protected Object preProcessCacheValue(@Nullable Object value) {
    return value != null ? value : isAllowNullValues() ? NullValue.INSTANCE : null;
  }

  /**
   * Customization hook for creating cache key before it gets serialized.
   *
   * @param key will never be {@literal null}.
   * @return never {@literal null}.
   */
  protected String cacheKey(Object key) {
    String convertedKey = convertKey(key);
    return configuration.getKeyFunction().compute(getName(), convertedKey);
  }

  /**
   * Convert {@code key} to a {@link String} used in cache key creation.
   *
   * @param key will never be {@literal null}.
   * @return never {@literal null}.
   * @throws IllegalStateException if {@code key} cannot be converted to {@link String}.
   */
  protected String convertKey(Object key) {

    if (key instanceof String stringKey) {
      return stringKey;
    }

    TypeDescriptor source = TypeDescriptor.forObject(key);

    ConversionService conversionService = configuration.getConversionService();

    if (conversionService.canConvert(source, TypeDescriptor.valueOf(String.class))) {
      try {
        return conversionService.convert(key, String.class);
      } catch (ConversionFailedException ex) {

        // May fail if the given key is a collection
        if (isCollectionLikeOrMap(source)) {
          return convertCollectionLikeOrMapKey(key, source);
        }

        throw ex;
      }
    }

    if (hasToStringMethod(key)) {
      return key.toString();
    }

    String message = String.format(
        "Cannot convert cache key %s to String; Please register a suitable Converter" + " via 'RedisCacheConfiguration.configureKeyConverters(...)' or override '%s.toString()'",
        source, key.getClass().getName());

    throw new IllegalStateException(message);
  }

  @Nullable
  private Object nullSafeDeserializedStoreValue(@Nullable Object value) {
    return value != null ? fromStoreValue(value) : null;
  }

  private boolean hasToStringMethod(Object target) {
    return hasToStringMethod(target.getClass());
  }

  private boolean hasToStringMethod(Class<?> type) {

    Method toString = ReflectionUtils.findMethod(type, "toString");

    return toString != null && !Object.class.equals(toString.getDeclaringClass());
  }

  private boolean isCollectionLikeOrMap(TypeDescriptor source) {
    return source.isArray() || source.isCollection() || source.isMap();
  }

  private String convertCollectionLikeOrMapKey(Object key, TypeDescriptor source) {

    if (source.isMap()) {

      int count = 0;

      StringBuilder target = new StringBuilder("{");

      for (Entry<?, ?> entry : ((Map<?, ?>) key).entrySet()) {
        target.append(convertKey(entry.getKey())).append("=").append(convertKey(entry.getValue()));
        target.append(++count > 1 ? ", " : "");
      }

      target.append("}");

      return target.toString();

    } else if (source.isCollection() || source.isArray()) {

      StringJoiner stringJoiner = new StringJoiner(",");

      Collection<?> collection = source.isCollection() ?
          (Collection<?>) key :
          Arrays.asList(ObjectUtils.toObjectArray(key));

      for (Object collectedKey : collection) {
        stringJoiner.add(convertKey(collectedKey));
      }

      return "[" + stringJoiner + "]";
    }

    throw new IllegalArgumentException(String.format("Cannot convert cache key [%s] to String", key));
  }

  private boolean nullCacheValueIsNotAllowed(@Nullable Object cacheValue) {
    return cacheValue == null && !isAllowNullValues();
  }

}
