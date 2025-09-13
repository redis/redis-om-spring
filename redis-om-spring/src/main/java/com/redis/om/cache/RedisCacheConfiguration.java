package com.redis.om.cache;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.redis.om.cache.common.RedisHashMapper;
import com.redis.om.cache.common.RedisStringMapper;
import com.redis.om.cache.common.mapping.GenericJackson2JsonMapper;
import com.redis.om.cache.common.mapping.JdkSerializationStringMapper;
import com.redis.om.cache.common.mapping.ObjectHashMapper;

import io.lettuce.core.AbstractRedisClient;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

/**
 * Configuration class for Redis Cache settings.
 * This class provides a fluent API for configuring various aspects of Redis caching,
 * including key generation, TTL, serialization, and Redis data types.
 */
public class RedisCacheConfiguration implements Cloneable {

  /**
   * Default TTL function that creates persistent entries (no expiration).
   */
  public static final TtlFunction DEFAULT_TTL_FUNCTION = TtlFunction.PERSISTENT;

  /**
   * Default key function that uses a simple key generation strategy.
   */
  public static final KeyFunction DEFAULT_KEY_FUNCTION = KeyFunction.SIMPLE;

  /**
   * Default Redis data type (HASH) used for storing cache entries.
   */
  public static final RedisType DEFAULT_REDIS_TYPE = RedisType.HASH;

  /**
   * Default mapper for converting objects to JSON format.
   */
  public static final RedisStringMapper DEFAULT_JSON_MAPPER = jsonStringMapper();

  /**
   * Default mapper for converting objects to String format using Java serialization.
   */
  public static final RedisStringMapper DEFAULT_STRING_MAPPER = javaStringMapper();

  /**
   * Default mapper for converting objects to Redis Hash entries.
   */
  public static final RedisHashMapper DEFAULT_HASH_MAPPER = new ObjectHashMapper();

  /**
   * Default expireOnGet value
   */
  public static final boolean DEFAULT_EXPIRE_ON_GET = true;

  private AbstractRedisClient client;

  private RedisType redisType = DEFAULT_REDIS_TYPE;

  private ConversionService conversionService = defaultConversionService();

  private KeyFunction keyFunction = DEFAULT_KEY_FUNCTION;

  private TtlFunction ttlFunction = DEFAULT_TTL_FUNCTION;

  private boolean expireOnGet = DEFAULT_EXPIRE_ON_GET;

  private RedisHashMapper hashMapper = DEFAULT_HASH_MAPPER;

  private RedisStringMapper stringMapper = DEFAULT_STRING_MAPPER;

  private RedisStringMapper jsonMapper = DEFAULT_JSON_MAPPER;

  private Optional<Map<String, Object>> localCache = Optional.empty();

  private MeterRegistry meterRegistry = Metrics.globalRegistry;

  private boolean indexEnabled;

  private String indexName;

  static JdkSerializationStringMapper javaStringMapper() {
    return java(null);
  }

  static JdkSerializationStringMapper java(@Nullable ClassLoader classLoader) {
    return new JdkSerializationStringMapper(classLoader);
  }

  static GenericJackson2JsonMapper jsonStringMapper() {
    return new GenericJackson2JsonMapper();
  }

  /**
   * Creates a default configuration for Redis caching.
   * 
   * @return a new instance of {@link RedisCacheConfiguration} with default settings
   */
  public static RedisCacheConfiguration defaultConfig() {
    return new RedisCacheConfiguration();
  }

  /**
   * Prefix the {@link RedisCache#getName() cache name} with the given value. <br />
   * The generated cache key will be: {@code prefix + cache name + "::" + cache entry key}.
   *
   * @param prefix the prefix to prepend to the cache name.
   * @return new {@link RedisCacheConfiguration}.
   * @see KeyFunction#prefixed(String)
   */
  public RedisCacheConfiguration keyPrefix(String prefix) {
    return keyFunction(KeyFunction.prefixed(prefix));
  }

  /**
   * Use the given {@link KeyFunction} to compute the Redis {@literal key} given the {@literal cache name} and
   * {@literal key}
   * as function inputs.
   *
   * @param function must not be {@literal null}.
   * @return new {@link RedisCacheConfiguration}.
   * @see KeyFunction
   */
  RedisCacheConfiguration keyFunction(KeyFunction function) {
    Assert.notNull(keyFunction, "Function used to compute cache key must not be null");
    return clone(config -> config.keyFunction = function);
  }

  /**
   * Configure the Redis data type to use for the cache.
   *
   * @param type the Redis data type to use
   * @return new {@link RedisCacheConfiguration} with the specified Redis data type
   */
  public RedisCacheConfiguration redisType(RedisType type) {
    return clone(c -> c.redisType = type);
  }

  /**
   * Configure the cache to use Redis JSON data type.
   *
   * @return new {@link RedisCacheConfiguration} with Redis JSON data type
   */
  public RedisCacheConfiguration json() {
    return redisType(RedisType.JSON);
  }

  /**
   * Configure the cache to use Redis Hash data type.
   *
   * @return new {@link RedisCacheConfiguration} with Redis Hash data type
   */
  public RedisCacheConfiguration hash() {
    return redisType(RedisType.HASH);
  }

  /**
   * Configure the cache to use Redis String data type.
   *
   * @return new {@link RedisCacheConfiguration} with Redis String data type
   */
  public RedisCacheConfiguration string() {
    return redisType(RedisType.STRING);
  }

  @Override
  public int hashCode() {
    return Objects.hash(client, conversionService, hashMapper, jsonMapper, keyFunction, localCache, meterRegistry,
        redisType, stringMapper, ttlFunction, expireOnGet);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RedisCacheConfiguration other = (RedisCacheConfiguration) obj;
    return Objects.equals(client, other.client) && Objects.equals(conversionService, other.conversionService) && Objects
        .equals(hashMapper, other.hashMapper) && Objects.equals(jsonMapper, other.jsonMapper) && Objects.equals(
            keyFunction, other.keyFunction) && Objects.equals(localCache, other.localCache) && Objects.equals(
                meterRegistry, other.meterRegistry) && redisType == other.redisType && Objects.equals(stringMapper,
                    other.stringMapper) && Objects.equals(ttlFunction,
                        other.ttlFunction) && expireOnGet == other.expireOnGet;
  }

  /**
   * Disable using cache key prefixes. <br />
   * <strong>NOTE</strong>: {@link Cache#clear()} might result in unintended removal of {@literal key}s in Redis. Make
   * sure to
   * use a dedicated Redis instance when disabling prefixes.
   *
   * @return new {@link RedisCacheConfiguration}.
   */
  public RedisCacheConfiguration disableKeyPrefix() {
    return clone(config -> config.keyFunction = KeyFunction.PASSTHROUGH);
  }

  private RedisCacheConfiguration clone(Consumer<RedisCacheConfiguration> consumer) {
    RedisCacheConfiguration config = clone();
    consumer.accept(config);
    return config;
  }

  @Override
  public RedisCacheConfiguration clone() {
    RedisCacheConfiguration config = new RedisCacheConfiguration();
    config.client = this.client;
    config.conversionService = this.conversionService;
    config.hashMapper = this.hashMapper;
    config.jsonMapper = this.jsonMapper;
    config.keyFunction = this.keyFunction;
    config.localCache = this.localCache;
    config.meterRegistry = this.meterRegistry;
    config.redisType = this.redisType;
    config.stringMapper = this.stringMapper;
    config.ttlFunction = this.ttlFunction;
    config.expireOnGet = this.expireOnGet;
    config.indexEnabled = this.indexEnabled;
    config.indexName = this.indexName;
    return config;
  }

  /**
   * Configure the meter registry to use for metrics collection.
   *
   * @param registry the meter registry to use
   * @return new {@link RedisCacheConfiguration} with the configured meter registry
   */
  public RedisCacheConfiguration meterRegistry(MeterRegistry registry) {
    return clone(config -> config.meterRegistry = registry);
  }

  /**
   * Enable or disable indexing for the cache.
   *
   * @param enable whether to enable indexing
   * @return new {@link RedisCacheConfiguration} with indexing enabled or disabled
   */
  public RedisCacheConfiguration indexEnabled(boolean enable) {
    return clone(config -> config.indexEnabled = enable);
  }

  /**
   * Configure the name of the index to use.
   *
   * @param index the name of the index
   * @return new {@link RedisCacheConfiguration} with the configured index name
   */
  public RedisCacheConfiguration indexName(String index) {
    return clone(config -> config.indexName = index);
  }

  /**
   * Set the ttl to apply for cache entries. Use {@link Duration#ZERO} to declare an eternal cache.
   *
   * @param ttl must not be {@literal null}.
   * @return new {@link RedisCacheConfiguration}.
   */
  public RedisCacheConfiguration entryTtl(Duration ttl) {
    Assert.notNull(ttl, "TTL duration must not be null");
    return entryTtl(TtlFunction.just(ttl));
  }

  /**
   *
   * @param enable if true, will set expiration timeout when key is read
   * @return new {@link RedisCacheConfiguration} with the configured expireOnGet
   */
  public RedisCacheConfiguration expireOnGet(boolean enable) {
    return clone(config -> config.expireOnGet = enable);
  }

  /**
   * Configure a local cache to use alongside Redis.
   *
   * @param cache the map to use as local cache
   * @return new {@link RedisCacheConfiguration} with the configured local cache
   */
  public RedisCacheConfiguration localCache(Map<String, Object> cache) {
    return clone(config -> config.localCache = Optional.of(cache));
  }

  /**
   * Set the {@link TtlFunction TTL function} to compute the time to live for cache entries.
   *
   * @param function the {@link TtlFunction} to compute the time to live for cache entries, must not be {@literal null}.
   * @return new {@link RedisCacheConfiguration}.
   */
  public RedisCacheConfiguration entryTtl(TtlFunction function) {
    Assert.notNull(function, "TtlFunction must not be null");
    return clone(config -> config.ttlFunction = function);
  }

  /**
   * Define the {@link ConversionService} used for cache key to {@link String} conversion.
   *
   * @param conversionService must not be {@literal null}.
   * @return new {@link RedisCacheConfiguration}.
   */
  public RedisCacheConfiguration conversionService(ConversionService conversionService) {
    Assert.notNull(conversionService, "ConversionService must not be null");
    return clone(config -> config.conversionService = conversionService);
  }

  /**
   * Configure the Redis client to use for cache operations.
   *
   * @param client the Redis client to use
   * @return new {@link RedisCacheConfiguration} with the configured Redis client
   */
  public RedisCacheConfiguration client(AbstractRedisClient client) {
    return clone(config -> config.client = client);
  }

  /**
   * Configure the mapper to use for converting objects to Redis Hash values.
   *
   * @param mapper the mapper to use for converting objects to Redis Hash values
   * @return new {@link RedisCacheConfiguration} with the configured hash mapper
   */
  public RedisCacheConfiguration hashMapper(RedisHashMapper mapper) {
    return clone(config -> config.hashMapper = mapper);
  }

  /**
   * Configure the mapper to use for converting objects to Redis String values.
   *
   * @param mapper the mapper to use for converting objects to Redis String values
   * @return new {@link RedisCacheConfiguration} with the configured string mapper
   */
  public RedisCacheConfiguration stringMapper(RedisStringMapper mapper) {
    return clone(config -> config.stringMapper = mapper);
  }

  /**
   * Configure the mapper to use for converting objects to JSON values.
   *
   * @param mapper the mapper to use for converting objects to JSON values
   * @return new {@link RedisCacheConfiguration} with the configured JSON mapper
   */
  public RedisCacheConfiguration jsonMapper(RedisStringMapper mapper) {
    return clone(config -> config.jsonMapper = mapper);
  }

  /**
   * Returns the Redis data type configured for this cache.
   * 
   * @return the configured {@link RedisType}
   */
  public RedisType getRedisType() {
    return redisType;
  }

  /**
   * Returns the local cache configuration if one is configured.
   * 
   * @return an {@link Optional} containing the local cache map, or empty if no local cache is configured
   */
  public Optional<Map<String, Object>> getLocalCache() {
    return localCache;
  }

  /**
   * @return The {@link ConversionService} used for cache key to {@link String} conversion. Never {@literal null}.
   */
  public ConversionService getConversionService() {
    return this.conversionService;
  }

  /**
   * Returns the Redis client configured for this cache.
   * 
   * @return the configured Redis client
   */
  public AbstractRedisClient getClient() {
    return client;
  }

  /**
   * Returns the mapper used for converting objects to Redis Hash entries.
   * 
   * @return the configured {@link RedisHashMapper}
   */
  public RedisHashMapper getHashMapper() {
    return hashMapper;
  }

  /**
   * Returns the mapper used for converting objects to JSON format.
   * 
   * @return the configured JSON {@link RedisStringMapper}
   */
  public RedisStringMapper getJsonMapper() {
    return jsonMapper;
  }

  /**
   * Returns the mapper used for converting objects to String format.
   * 
   * @return the configured {@link RedisStringMapper} for String serialization
   */
  public RedisStringMapper getStringMapper() {
    return stringMapper;
  }

  /**
   * Gets the {@link TtlFunction} used to compute a cache key {@literal time-to-live (TTL) expiration}.
   *
   * @return the {@link TtlFunction} used to compute expiration time (TTL) for cache entries; never {@literal null}.
   */
  public TtlFunction getTtlFunction() {
    return this.ttlFunction;
  }

  /**
   *
   * @return true if expireOnGet is enabled, false otherwise
   */
  public boolean isExpireOnGet() {
    return expireOnGet;
  }

  /**
   * Returns the {@link KeyFunction} used to compute a cache key.
   * 
   * @return the {@link KeyFunction} used to compute keys for cache entries; never {@literal null}.
   */
  public KeyFunction getKeyFunction() {
    return keyFunction;
  }

  /**
   * Returns the meter registry used for metrics collection.
   * 
   * @return the configured {@link MeterRegistry}
   */
  public MeterRegistry getMeterRegistry() {
    return meterRegistry;
  }

  /**
   * Returns whether indexing is enabled for this cache.
   * 
   * @return {@code true} if indexing is enabled, {@code false} otherwise
   */
  public boolean isIndexEnabled() {
    return indexEnabled;
  }

  /**
   * Returns the name of the index configured for this cache.
   * 
   * @return the configured index name
   */
  public String getIndexName() {
    return indexName;
  }

  /**
   * Adds a {@link Converter} to extract the {@link String} representation of a {@literal cache key} if no suitable
   * {@link Object#toString()} method is present.
   *
   * @param cacheKeyConverter {@link Converter} used to convert a {@literal cache key} into a {@link String}.
   * @throws IllegalStateException if {@link #getConversionService()} does not allow {@link Converter} registration.
   * @see Converter
   */
  public void addCacheKeyConverter(Converter<?, String> cacheKeyConverter) {
    configureKeyConverters(it -> it.addConverter(cacheKeyConverter));
  }

  /**
   * Configure the underlying {@link ConversionService} used to extract the {@literal cache key}.
   *
   * @param registryConsumer {@link Consumer} used to register a {@link Converter} with the configured
   *                         {@link ConverterRegistry}; never {@literal null}.
   * @throws IllegalStateException if {@link #getConversionService()} does not allow {@link Converter} registration.
   * @see ConverterRegistry
   */
  public void configureKeyConverters(Consumer<ConverterRegistry> registryConsumer) {
    if (!(getConversionService() instanceof ConverterRegistry)) {

      String message = "'%s' returned by getConversionService() does not allow Converter registration;" + " Please make sure to provide a ConversionService that implements ConverterRegistry";

      throw new IllegalStateException(String.format(message, getConversionService().getClass().getName()));
    }
    registryConsumer.accept((ConverterRegistry) getConversionService());
  }

  /**
   * Registers default cache {@link Converter key converters}.
   * 
   * The following converters get registered:
   * 
   * <ul>
   * <li>{@link String} to byte[] using UTF-8 encoding.</li>
   * <li>{@link SimpleKey} to {@link String}</li>
   * </ul>
   *
   * @param registry {@link ConverterRegistry} in which the {@link Converter key converters} are registered; must not be
   *                 {@literal null}.
   * @see ConverterRegistry
   */
  public static void registerDefaultConverters(ConverterRegistry registry) {

    Assert.notNull(registry, "ConverterRegistry must not be null");

    registry.addConverter(String.class, byte[].class, source -> source.getBytes(StandardCharsets.UTF_8));
    registry.addConverter(SimpleKey.class, String.class, SimpleKey::toString);
  }

  /**
   * Creates a default conversion service with the standard converters registered.
   * 
   * @return a new {@link ConversionService} with default converters
   */
  public static ConversionService defaultConversionService() {
    DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
    registerDefaultConverters(conversionService);
    return conversionService;
  }

}
