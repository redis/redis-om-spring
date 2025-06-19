package com.redis.om.cache;

import java.time.Duration;

import org.springframework.lang.Nullable;

/**
 * {@link CacheAccessor} provides low-level access to Redis commands
 * ({@code HSET, HGETALL, EXPIRE,...}) used for caching.
 * <p>
 * The {@link CacheAccessor} may be shared by multiple cache implementations and
 * is responsible for reading/writing binary data from/to Redis. The
 * implementation honors potential cache lock flags that might be set.
 */
public interface CacheAccessor {

  /**
   * Get the binary value representation from Redis stored for the given key and
   * set the given {@link Duration TTL expiration} for the cache entry.
   *
   * @param key must not be {@literal null}.
   * @param ttl {@link Duration} specifying the {@literal expiration timeout} for
   *            the cache entry.
   * @return {@literal null} if key does not exist or has {@literal expired}.
   */
  @Nullable
  Object get(String key, @Nullable Duration ttl);

  /**
   * Write the given key/value pair to Redis and set the expiration time if
   * defined.
   *
   * @param key   The key for the cache entry. Must not be {@literal null}.
   * @param value The value stored for the key. Must not be {@literal null}.
   * @param ttl   Optional expiration time. Can be {@literal null}.
   */
  void put(String key, Object value, @Nullable Duration ttl);

  /**
   * Write the given value to Redis if the key does not already exist.
   *
   * @param key   The key for the cache entry. Must not be {@literal null}.
   * @param value The value stored for the key. Must not be {@literal null}.
   * @param ttl   Optional expiration time. Can be {@literal null}.
   * @return {@literal null} if the value has been written, the value stored for
   *         the key if it already exists.
   */
  @Nullable
  Object putIfAbsent(String key, Object value, @Nullable Duration ttl);

  /**
   * Remove the given key from Redis.
   *
   * @param key The key for the cache entry. Must not be {@literal null}.
   */
  void remove(String key);

  /**
   * Remove all keys following the given pattern.
   *
   * @param pattern The pattern for the keys to remove. Must not be
   *                {@literal null}.
   * @return number of keys deleted
   */
  long clean(String pattern);

}
