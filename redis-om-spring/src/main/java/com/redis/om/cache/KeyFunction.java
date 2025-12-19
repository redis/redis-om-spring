package com.redis.om.cache;

import org.springframework.util.Assert;

/**
 * {@link KeyFunction} is a function for creating custom prefixes prepended to
 * the actual {@literal key} stored in Redis.
 *
 */
@FunctionalInterface
public interface KeyFunction {

  /**
   * Default separator.
   *
   */
  String SEPARATOR = ":";

  /**
   * A pass-through implementation that returns the key unchanged without any prefix.
   * This can be used when no key transformation is needed.
   */
  KeyFunction PASSTHROUGH = (cache, key) -> key;

  /**
   * Default {@link KeyFunction} scheme that prefixes cache keys with
   * the {@link String name} of the cache followed by double colons.
   *
   * For example, a cache named {@literal myCache} will prefix all cache keys with
   * {@literal myCache::}.
   *
   */
  KeyFunction SIMPLE = (cache, key) -> cache + SEPARATOR + key;

  /**
   * Compute the {@link String prefix} for the actual {@literal cache key} stored
   * in Redis.
   *
   * @param cache {@link String name} of the cache in which the key is stored;
   *              will never be {@literal null}.
   * @param key   the cache key to be processed; will never be {@literal null}.
   * @return the computed {@literal cache key} stored in Redis; never
   *         {@literal null}.
   */
  String compute(String cache, String key);

  /**
   * Creates a {@link KeyFunction} scheme that prefixes cache keys with the given
   * {@link String prefix}.
   *
   * The {@link String prefix} is prepended to the {@link String cacheName}
   * followed by double colons.
   *
   * For example, a prefix {@literal redis-} with a cache named
   * {@literal  myCache} results in {@literal  redis-myCache::}.
   *
   * @param prefix must not be {@literal null}.
   * @return the default {@link KeyFunction} scheme.
   * @since 2.3
   */
  static KeyFunction prefixed(String prefix) {
    Assert.notNull(prefix, "Prefix must not be null");
    return (name, key) -> prefix + name + SEPARATOR + key;
  }

}
