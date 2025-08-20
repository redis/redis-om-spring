package com.redis.om.cache;

import java.util.*;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import io.lettuce.core.AbstractRedisClient;

/**
 * {@link CacheManager} implementation for Redis backed by {@link RedisCache}.
 * <p>
 * This {@link CacheManager} creates {@link Cache caches} on first write, by default. Empty {@link Cache caches} are not
 * visible
 * in Redis due to how Redis represents empty data structures.
 * <p>
 * {@link Cache Caches} requiring a different {@link RedisCacheConfiguration cache configuration} than the default cache
 * configuration} can be specified via {@link RedisCacheManagerBuilder#initialConfigurations(Map)} or individually using
 * {@link RedisCacheManagerBuilder#configuration(String, RedisCacheConfiguration)}.
 *
 * @see AbstractTransactionSupportingCacheManager
 */
public class RedisCacheManager extends AbstractTransactionSupportingCacheManager {

  /**
   * Default setting for allowing runtime cache creation.
   */
  protected static final boolean DEFAULT_ALLOW_RUNTIME_CACHE_CREATION = true;

  private final boolean allowRuntimeCacheCreation;

  private final RedisCacheConfiguration defaultCacheConfiguration;

  private final AbstractRedisClient client;

  private final Map<String, RedisCacheConfiguration> initialCacheConfiguration;

  /**
   * Creates a new {@link RedisCacheManager} initialized with the given {@link AbstractRedisClient} and default
   * {@link RedisCacheConfiguration}.
   * <p>
   * Allows {@link RedisCache cache} creation at runtime.
   *
   * @param client                    {@link AbstractRedisClient} used to perform {@link RedisCache} operations by
   *                                  executing appropriate Redis
   *                                  commands; must not be {@literal null}.
   * @param defaultCacheConfiguration {@link RedisCacheConfiguration} applied to new {@link RedisCache Redis caches} by
   *                                  default when no cache-specific {@link RedisCacheConfiguration} is provided; must
   *                                  not be {@literal null}.
   * @throws IllegalArgumentException if either the given {@link AbstractRedisClient} or {@link RedisCacheConfiguration}
   *                                  are
   *                                  {@literal null}.
   */
  public RedisCacheManager(AbstractRedisClient client, RedisCacheConfiguration defaultCacheConfiguration) {
    this(client, defaultCacheConfiguration, DEFAULT_ALLOW_RUNTIME_CACHE_CREATION);
  }

  /**
   * Creates a new {@link RedisCacheManager} initialized with the given {@link AbstractRedisClient} and default
   * {@link RedisCacheConfiguration} along with whether to allow cache creation at runtime.
   *
   * @param client                    {@link AbstractRedisClient} used to perform {@link RedisCache} operations by
   *                                  executing appropriate Redis
   *                                  commands; must not be {@literal null}.
   * @param defaultCacheConfiguration {@link RedisCacheConfiguration} applied to new {@link RedisCache Redis caches} by
   *                                  default when no cache-specific {@link RedisCacheConfiguration} is provided; must
   *                                  not be {@literal null}.
   * @param allowRuntimeCacheCreation boolean specifying whether to allow creation of undeclared caches at runtime;
   *                                  {@literal true} by default.
   * @throws IllegalArgumentException if either the given {@link AbstractRedisClient} or {@link RedisCacheConfiguration}
   *                                  are
   *                                  {@literal null}.
   */
  private RedisCacheManager(AbstractRedisClient client, RedisCacheConfiguration defaultCacheConfiguration,
      boolean allowRuntimeCacheCreation) {
    Assert.notNull(defaultCacheConfiguration, "DefaultCacheConfiguration must not be null");
    this.defaultCacheConfiguration = defaultCacheConfiguration;
    Assert.notNull(client, "Client must not be null");
    this.client = client;
    this.initialCacheConfiguration = new LinkedHashMap<>();
    this.allowRuntimeCacheCreation = allowRuntimeCacheCreation;
  }

  /**
   * Creates a new {@link RedisCacheManager} initialized with the given {@link AbstractRedisClient} and a default
   * {@link RedisCacheConfiguration} along with an optional, initial set of {@link String cache names} used to create
   * {@link RedisCache Redis caches} on startup.
   * <p>
   * Allows {@link RedisCache cache} creation at runtime.
   *
   * @param client                    {@link AbstractRedisClient} used to perform {@link RedisCache} operations by
   *                                  executing appropriate Redis
   *                                  commands; must not be {@literal null}.
   * @param defaultCacheConfiguration {@link RedisCacheConfiguration} applied to new {@link RedisCache Redis caches} by
   *                                  default when no cache-specific {@link RedisCacheConfiguration} is provided; must
   *                                  not be {@literal null}.
   * @param initialCacheNames         optional set of {@link String cache names} used to create {@link RedisCache Redis
   *                                  caches} on
   *                                  startup. The default {@link RedisCacheConfiguration} will be applied to each
   *                                  cache.
   * @throws IllegalArgumentException if either the given {@link AbstractRedisClient} or {@link RedisCacheConfiguration}
   *                                  are
   *                                  {@literal null}.
   */
  public RedisCacheManager(AbstractRedisClient client, RedisCacheConfiguration defaultCacheConfiguration,
      String... initialCacheNames) {

    this(client, defaultCacheConfiguration, DEFAULT_ALLOW_RUNTIME_CACHE_CREATION, initialCacheNames);
  }

  /**
   * Creates a new {@link RedisCacheManager} initialized with the given {@link AbstractRedisClient} and default
   * {@link RedisCacheConfiguration} along with whether to allow cache creation at runtime.
   * <p>
   * Additionally, the optional, initial set of {@link String cache names} will be used to create {@link RedisCache
   * Redis
   * caches} on startup.
   *
   * @param client                    {@link AbstractRedisClient} used to perform {@link RedisCache} operations by
   *                                  executing appropriate Redis
   *                                  commands; must not be {@literal null}.
   * @param defaultCacheConfiguration {@link RedisCacheConfiguration} applied to new {@link RedisCache Redis caches} by
   *                                  default when no cache-specific {@link RedisCacheConfiguration} is provided; must
   *                                  not be {@literal null}.
   * @param allowRuntimeCacheCreation boolean specifying whether to allow creation of undeclared caches at runtime;
   *                                  {@literal true} by default.
   * @param initialCacheNames         optional set of {@link String cache names} used to create {@link RedisCache Redis
   *                                  caches} on
   *                                  startup. The default {@link RedisCacheConfiguration} will be applied to each
   *                                  cache.
   * @throws IllegalArgumentException if either the given {@link AbstractRedisClient} or {@link RedisCacheConfiguration}
   *                                  are
   *                                  {@literal null}.
   */
  public RedisCacheManager(AbstractRedisClient client, RedisCacheConfiguration defaultCacheConfiguration,
      boolean allowRuntimeCacheCreation, String... initialCacheNames) {

    this(client, defaultCacheConfiguration, allowRuntimeCacheCreation);

    for (String cacheName : initialCacheNames) {
      this.initialCacheConfiguration.put(cacheName, defaultCacheConfiguration);
    }
  }

  /**
   * Creates new {@link RedisCacheManager} using given {@link AbstractRedisClient} and default
   * {@link RedisCacheConfiguration}.
   * <p>
   * Additionally, an initial {@link RedisCache} will be created and configured using the associated
   * {@link RedisCacheConfiguration} for each {@link String named} {@link RedisCache} in the given {@link Map}.
   * <p>
   * Allows {@link RedisCache cache} creation at runtime.
   *
   * @param client                     {@link AbstractRedisClient} used to perform {@link RedisCache} operations by
   *                                   executing appropriate Redis
   *                                   commands; must not be {@literal null}.
   * @param defaultCacheConfiguration  {@link RedisCacheConfiguration} applied to new {@link RedisCache Redis caches} by
   *                                   default when no cache-specific {@link RedisCacheConfiguration} is provided; must
   *                                   not be {@literal null}.
   * @param initialCacheConfigurations {@link Map} of declared, known {@link String cache names} along with associated
   *                                   {@link RedisCacheConfiguration} used to create and configure {@link RedisCache
   *                                   Reds caches} on startup; must not
   *                                   be {@literal null}.
   * @throws IllegalArgumentException if either the given {@link AbstractRedisClient} or {@link RedisCacheConfiguration}
   *                                  are
   *                                  {@literal null}.
   */
  public RedisCacheManager(AbstractRedisClient client, RedisCacheConfiguration defaultCacheConfiguration,
      Map<String, RedisCacheConfiguration> initialCacheConfigurations) {

    this(client, defaultCacheConfiguration, DEFAULT_ALLOW_RUNTIME_CACHE_CREATION, initialCacheConfigurations);
  }

  /**
   * Creates a new {@link RedisCacheManager} initialized with the given {@link AbstractRedisClient} and a default
   * {@link RedisCacheConfiguration}, and whether to allow {@link RedisCache} creation at runtime.
   * <p>
   * Additionally, an initial {@link RedisCache} will be created and configured using the associated
   * {@link RedisCacheConfiguration} for each {@link String named} {@link RedisCache} in the given {@link Map}.
   *
   * @param client                     {@link AbstractRedisClient} used to perform {@link RedisCache} operations by
   *                                   executing appropriate Redis
   *                                   commands; must not be {@literal null}.
   * @param defaultCacheConfiguration  {@link RedisCacheConfiguration} applied to new {@link RedisCache Redis caches} by
   *                                   default when no cache-specific {@link RedisCacheConfiguration} is provided; must
   *                                   not be {@literal null}.
   * @param allowRuntimeCacheCreation  boolean specifying whether to allow creation of undeclared caches at runtime;
   *                                   {@literal true} by default.
   * @param initialCacheConfigurations {@link Map} of declared, known {@link String cache names} along with the
   *                                   associated
   *                                   {@link RedisCacheConfiguration} used to create and configure {@link RedisCache
   *                                   Redis caches} on startup; must not
   *                                   be {@literal null}.
   * @throws IllegalArgumentException if either the given {@link AbstractRedisClient} or {@link RedisCacheConfiguration}
   *                                  are
   *                                  {@literal null}.
   */
  public RedisCacheManager(AbstractRedisClient client, RedisCacheConfiguration defaultCacheConfiguration,
      boolean allowRuntimeCacheCreation, Map<String, RedisCacheConfiguration> initialCacheConfigurations) {

    this(client, defaultCacheConfiguration, allowRuntimeCacheCreation);

    Assert.notNull(initialCacheConfigurations, "InitialCacheConfigurations must not be null");

    this.initialCacheConfiguration.putAll(initialCacheConfigurations);
  }

  /**
   * Factory method returning a {@literal Builder} used to construct and configure a {@link RedisCacheManager}.
   *
   * @return new {@link RedisCacheManagerBuilder}.
   */
  public static RedisCacheManagerBuilder builder() {
    return new RedisCacheManagerBuilder();
  }

  /**
   * Factory method returning a {@literal Builder} used to construct and configure a {@link RedisCacheManager}
   * initialized
   * with the given {@link AbstractRedisClient}.
   *
   * @param client {@link AbstractRedisClient} used by the {@link RedisCacheManager} to acquire connections to Redis
   *               when
   *               performing {@link RedisCache} operations; must not be {@literal null}.
   * @return new {@link RedisCacheManagerBuilder}.
   * @throws IllegalArgumentException if the given {@link AbstractRedisClient} is {@literal null}.
   */
  public static RedisCacheManagerBuilder builder(AbstractRedisClient client) {

    Assert.notNull(client, "Client must not be null");

    return RedisCacheManagerBuilder.fromClient(client);
  }

  /**
   * Factory method used to construct a new {@link RedisCacheManager} initialized with the given
   * {@link AbstractRedisClient}
   * and using defaults for caching.
   * <dl>
   * <dt>initial caches</dt>
   * <dd>none</dd>
   * <dt>in-flight cache creation</dt>
   * <dd>enabled</dd>
   * </dl>
   *
   * @param client {@link AbstractRedisClient} used by the {@link RedisCacheManager} to acquire connections to Redis
   *               when
   *               performing {@link RedisCache} operations; must not be {@literal null}.
   * @return new {@link RedisCacheManager}.
   * @throws IllegalArgumentException if the given {@link AbstractRedisClient} is {@literal null}.
   */
  public static RedisCacheManager create(AbstractRedisClient client) {
    Assert.notNull(client, "Client must not be null");
    return new RedisCacheManager(client, new RedisCacheConfiguration());
  }

  /**
   * Determines whether {@link RedisCache Redis caches} are allowed to be created at runtime.
   *
   * @return a boolean value indicating whether {@link RedisCache Redis caches} are allowed to be created at runtime.
   */
  public boolean isAllowRuntimeCacheCreation() {
    return this.allowRuntimeCacheCreation;
  }

  /**
   * Return an {@link Collections#unmodifiableMap(Map) unmodifiable Map} containing {@link String caches name} mapped to
   * the
   * {@link RedisCache} {@link RedisCacheConfiguration configuration}.
   *
   * @return unmodifiable {@link Map} containing {@link String cache name} / {@link RedisCacheConfiguration
   *         configuration}
   *         pairs.
   */
  public Map<String, RedisCacheConfiguration> getCacheConfigurations() {

    Map<String, RedisCacheConfiguration> cacheConfigurationMap = new HashMap<>(getCacheNames().size());

    getCacheNames().forEach(cacheName -> {
      RedisCache cache = (RedisCache) lookupCache(cacheName);
      RedisCacheConfiguration cacheConfiguration = cache != null ? cache.getConfiguration() : null;
      cacheConfigurationMap.put(cacheName, cacheConfiguration);
    });

    return Collections.unmodifiableMap(cacheConfigurationMap);
  }

  /**
   * Gets the default {@link RedisCacheConfiguration} applied to new {@link RedisCache} instances on creation when
   * custom,
   * non-specific {@link RedisCacheConfiguration} was not provided.
   *
   * @return the default {@link RedisCacheConfiguration}.
   */
  protected RedisCacheConfiguration getDefaultCacheConfiguration() {
    return this.defaultCacheConfiguration;
  }

  /**
   * Gets a {@link Map} of {@link String cache names} to {@link RedisCacheConfiguration} objects as the initial set of
   * {@link RedisCache Redis caches} to create on startup.
   *
   * @return a {@link Map} of {@link String cache names} to {@link RedisCacheConfiguration} objects.
   */
  protected Map<String, RedisCacheConfiguration> getInitialCacheConfiguration() {
    return Collections.unmodifiableMap(this.initialCacheConfiguration);
  }

  /**
   * Adds a cache configuration to this cache manager.
   * 
   * @param name          the name of the cache
   * @param configuration the cache configuration to use
   */
  public void addCacheConfiguration(String name, RedisCacheConfiguration configuration) {
    this.initialCacheConfiguration.put(name, configuration);
  }

  @Override
  public RedisCache getMissingCache(String name) {
    return isAllowRuntimeCacheCreation() ? createRedisCache(name, getDefaultCacheConfiguration()) : null;
  }

  /**
   * Creates a new {@link RedisCache} with given {@link String name} and {@link RedisCacheConfiguration}.
   *
   * @param name               {@link String name} for the {@link RedisCache}; must not be {@literal null}.
   * @param cacheConfiguration {@link RedisCacheConfiguration} used to configure the {@link RedisCache}; resolves to the
   *                           {@link #getDefaultCacheConfiguration()} if {@literal null}.
   * @return a new {@link RedisCache} instance; never {@literal null}.
   */
  protected RedisCache createRedisCache(String name, @Nullable RedisCacheConfiguration cacheConfiguration) {
    return new RedisCache(name, client, resolveCacheConfiguration(cacheConfiguration));
  }

  @Override
  protected Collection<RedisCache> loadCaches() {

    return getInitialCacheConfiguration().entrySet().stream().map(entry -> createRedisCache(entry.getKey(), entry
        .getValue())).toList();
  }

  private RedisCacheConfiguration resolveCacheConfiguration(@Nullable RedisCacheConfiguration cacheConfiguration) {
    return cacheConfiguration != null ? cacheConfiguration : getDefaultCacheConfiguration();
  }

  /**
   * {@literal Builder} for creating a {@link RedisCacheManager}.
   *
   */
  public static class RedisCacheManagerBuilder {

    /**
     * Factory method returning a new {@literal Builder} used to create and configure a {@link RedisCacheManager} using
     * the
     * given {@link AbstractRedisClient}.
     *
     * @param client {@link AbstractRedisClient} used by the {@link RedisCacheManager} to acquire connections to Redis
     *               when
     *               performing {@link RedisCache} operations; must not be {@literal null}.
     * @return new {@link RedisCacheManagerBuilder}.
     * @throws IllegalArgumentException if the given {@link AbstractRedisClient} is {@literal null}.
     */
    public static RedisCacheManagerBuilder fromClient(AbstractRedisClient client) {

      Assert.notNull(client, "Client must not be null");

      return new RedisCacheManagerBuilder(client);
    }

    private boolean allowRuntimeCacheCreation = true;

    private final Map<String, RedisCacheConfiguration> initialCaches = new LinkedHashMap<>();

    private RedisCacheConfiguration defaultCacheConfiguration = new RedisCacheConfiguration();

    private @Nullable AbstractRedisClient client;

    private RedisCacheManagerBuilder() {
    }

    private RedisCacheManagerBuilder(AbstractRedisClient client) {
      this.client = client;
      ;
    }

    /**
     * Configure whether to allow cache creation at runtime.
     *
     * @param allowRuntimeCacheCreation boolean to allow creation of undeclared caches at runtime; {@literal true} by
     *                                  default.
     * @return this {@link RedisCacheManagerBuilder}.
     */
    public RedisCacheManagerBuilder allowCreateOnMissingCache(boolean allowRuntimeCacheCreation) {
      this.allowRuntimeCacheCreation = allowRuntimeCacheCreation;
      return this;
    }

    /**
     * Disable {@link RedisCache} creation at runtime for non-configured, undeclared caches.
     * <p>
     * {@link RedisCacheManager#getMissingCache(String)} returns {@literal null} for any non-configured, undeclared
     * {@link Cache} instead of a new {@link RedisCache} instance. This allows the
     * {@link org.springframework.cache.support.CompositeCacheManager} to participate.
     *
     * @return this {@link RedisCacheManagerBuilder}.
     * @see #allowCreateOnMissingCache(boolean)
     * @see #enableCreateOnMissingCache()
     */
    public RedisCacheManagerBuilder disableCreateOnMissingCache() {
      return allowCreateOnMissingCache(false);
    }

    /**
     * Enables {@link RedisCache} creation at runtime for unconfigured, undeclared caches.
     *
     * @return this {@link RedisCacheManagerBuilder}.
     * @see #allowCreateOnMissingCache(boolean)
     * @see #disableCreateOnMissingCache()
     */
    public RedisCacheManagerBuilder enableCreateOnMissingCache() {
      return allowCreateOnMissingCache(true);
    }

    /**
     * Returns the default {@link RedisCacheConfiguration}.
     *
     * @return the default {@link RedisCacheConfiguration}.
     */
    public RedisCacheConfiguration defaults() {
      return this.defaultCacheConfiguration;
    }

    /**
     * Define a default {@link RedisCacheConfiguration} applied to dynamically created {@link RedisCache}s.
     *
     * @param configuration must not be {@literal null}.
     * @return this {@link RedisCacheManagerBuilder}.
     */
    public RedisCacheManagerBuilder defaults(RedisCacheConfiguration configuration) {

      Assert.notNull(configuration, "DefaultCacheConfiguration must not be null");

      this.defaultCacheConfiguration = configuration;

      return this;
    }

    /**
     * Configure a {@link AbstractRedisClient}.
     *
     * @param client must not be {@literal null}.
     * @return this {@link RedisCacheManagerBuilder}.
     */
    public RedisCacheManagerBuilder client(AbstractRedisClient client) {
      Assert.notNull(client, "Client must not be null");
      this.client = client;
      return this;
    }

    /**
     * Append a {@link Set} of cache names to be pre initialized with current {@link RedisCacheConfiguration}.
     * <strong>NOTE:</strong> This calls depends on {@link #defaults(RedisCacheConfiguration)} using whatever default
     * {@link RedisCacheConfiguration} is present at the time of invoking this method.
     *
     * @param cacheNames must not be {@literal null}.
     * @return this {@link RedisCacheManagerBuilder}.
     */
    public RedisCacheManagerBuilder initialCacheNames(Set<String> cacheNames) {

      Assert.notNull(cacheNames, "CacheNames must not be null");
      cacheNames.forEach(it -> configuration(it, defaultCacheConfiguration));

      return this;
    }

    /**
     * Registers the given {@link String cache name} and {@link RedisCacheConfiguration} used to create and configure a
     * {@link RedisCache} on startup.
     *
     * @param cacheName          {@link String name} of the cache to register for creation on startup.
     * @param cacheConfiguration {@link RedisCacheConfiguration} used to configure the new cache on startup.
     * @return this {@link RedisCacheManagerBuilder}.
     */
    public RedisCacheManagerBuilder configuration(String cacheName, RedisCacheConfiguration cacheConfiguration) {

      Assert.notNull(cacheName, "CacheName must not be null");
      Assert.notNull(cacheConfiguration, "CacheConfiguration must not be null");

      this.initialCaches.put(cacheName, cacheConfiguration);

      return this;
    }

    /**
     * Append a {@link Map} of cache name/{@link RedisCacheConfiguration} pairs to be pre initialized.
     *
     * @param configs must not be {@literal null}.
     * @return this {@link RedisCacheManagerBuilder}.
     */
    public RedisCacheManagerBuilder initialConfigurations(Map<String, RedisCacheConfiguration> configs) {

      Assert.notNull(configs, "CacheConfigurations must not be null");
      configs.forEach((cacheName, cacheConfiguration) -> Assert.notNull(cacheConfiguration, String.format(
          "RedisCacheConfiguration for cache [%s] must not be null", cacheName)));

      this.initialCaches.putAll(configs);

      return this;
    }

    /**
     * Get the {@link RedisCacheConfiguration} for a given cache by its name.
     *
     * @param cacheName must not be {@literal null}.
     * @return {@link Optional#empty()} if no {@link RedisCacheConfiguration} set for the given cache name.
     */
    public Optional<RedisCacheConfiguration> getCacheConfigurationFor(String cacheName) {
      return Optional.ofNullable(this.initialCaches.get(cacheName));
    }

    /**
     * Get the {@link Set} of cache names for which the builder holds {@link RedisCacheConfiguration configuration}.
     *
     * @return an unmodifiable {@link Set} holding the name of caches for which a {@link RedisCacheConfiguration
     *         configuration} has been set.
     */
    public Set<String> getConfiguredCaches() {
      return Collections.unmodifiableSet(this.initialCaches.keySet());
    }

    /**
     * Create new instance of {@link RedisCacheManager} with configuration options applied.
     *
     * @return new instance of {@link RedisCacheManager}.
     */
    public RedisCacheManager build() {

      Assert.state(client != null,
          "Client must not be null;" + " You can provide one via 'RedisCacheManagerBuilder#cacheWriter(RedisCacheWriter)'");

      return newRedisCacheManager(client);
    }

    private RedisCacheManager newRedisCacheManager(AbstractRedisClient client) {
      return new RedisCacheManager(client, defaults(), this.allowRuntimeCacheCreation, this.initialCaches);
    }

  }

}
