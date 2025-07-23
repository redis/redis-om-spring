package com.redis.om.cache;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.redis.lettucemod.api.StatefulRedisModulesConnection;

import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanIterator;

/**
 * Abstract base class for Redis cache accessors that provides common functionality
 * for interacting with Redis as a cache.
 */
abstract class AbstractRedisCacheAccessor implements CacheAccessor {

  /**
   * Default number of elements to scan in each iteration when cleaning cache entries.
   */
  public static final long DEFAULT_SCAN_COUNT = 100;

  /**
   * The Redis connection used for cache operations.
   */
  protected final StatefulRedisModulesConnection<byte[], byte[]> connection;

  private long scanCount = DEFAULT_SCAN_COUNT;

  /**
   * Creates a new {@link AbstractRedisCacheAccessor} with the given connection.
   *
   * @param connection the Redis connection, must not be {@literal null}.
   */
  AbstractRedisCacheAccessor(StatefulRedisModulesConnection<byte[], byte[]> connection) {
    Assert.notNull(connection, "Connection must not be null");
    this.connection = connection;

  }

  /**
   * Converts a String key to a byte array using UTF-8 encoding.
   *
   * @param key the key to convert, never {@literal null}.
   * @return the key as a byte array.
   */
  private byte[] convertKey(String key) {
    return key.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public Object get(String key, Duration ttl) {
    return get(convertKey(key), ttl);
  }

  /**
   * Retrieves a cached object for the given key, optionally extending its TTL.
   *
   * @param key the cache key as byte array, never {@literal null}.
   * @param ttl the time-to-live to set if the key exists, can be {@literal null}.
   * @return the cached object or {@literal null} if not found.
   */
  protected abstract Object get(byte[] key, Duration ttl);

  @Override
  public void put(String key, Object value, Duration ttl) {
    put(convertKey(key), value, ttl);
  }

  /**
   * Stores an object in the cache with the given key and TTL.
   *
   * @param key   the cache key as byte array, never {@literal null}.
   * @param value the object to cache, can be {@literal null}.
   * @param ttl   the time-to-live for the cached entry, can be {@literal null}.
   */
  protected abstract void put(byte[] key, Object value, Duration ttl);

  @Override
  public Object putIfAbsent(String key, Object value, Duration ttl) {
    return putIfAbsent(convertKey(key), value, ttl);
  }

  /**
   * Stores an object in the cache only if the key does not already exist.
   *
   * @param key   the cache key as byte array, never {@literal null}.
   * @param value the object to cache, can be {@literal null}.
   * @param ttl   the time-to-live for the cached entry, can be {@literal null}.
   * @return the previous value associated with the key, or {@literal null} if there was no value.
   */
  protected abstract Object putIfAbsent(byte[] key, Object value, Duration ttl);

  /**
   * Sets the number of elements to scan in each iteration when cleaning cache entries.
   * 
   * @param count the number of elements to scan in each iteration
   */
  public void setScanCount(long count) {
    this.scanCount = count;
  }

  @Override
  public void remove(String key) {
    Assert.notNull(key, "Key must not be null");
    delete(convertKey(key));
  }

  @Override
  public long clean(String pattern) {
    Assert.notNull(pattern, "Pattern must not be null");
    ScanArgs args = new ScanArgs();
    args.match(pattern);
    args.limit(scanCount);
    ScanIterator<byte[]> scanIterator = ScanIterator.scan(connection.sync(), args);
    List<byte[]> keys = new ArrayList<>();
    long count = 0;
    while (scanIterator.hasNext()) {
      keys.add(scanIterator.next());
      if (keys.size() >= scanCount) {
        count += delete(keys);
        keys.clear();
      }
    }
    count += delete(keys);
    return count;
  }

  /**
   * Checks if a key exists in Redis.
   *
   * @param key the key to check, never {@literal null}.
   * @return {@literal true} if the key exists, {@literal false} otherwise.
   */
  protected boolean exists(byte[] key) {
    return connection.sync().exists(key) > 0;
  }

  /**
   * Deletes multiple keys from Redis.
   *
   * @param keys the list of keys to delete, can be empty.
   * @return the number of keys that were deleted.
   */
  private long delete(List<byte[]> keys) {
    if (CollectionUtils.isEmpty(keys)) {
      return 0;
    }
    return delete(keys.toArray(new byte[0][]));
  }

  /**
   * Deletes multiple keys from Redis.
   *
   * @param keys the array of keys to delete.
   * @return the number of keys that were deleted.
   */
  private long delete(byte[]... keys) {
    return connection.sync().del(keys);
  }

  /**
   * Determines if the given TTL duration should be applied as an expiration.
   * 
   * @param ttl the time-to-live duration, can be {@literal null}
   * @return {@literal true} if the TTL is not null, not zero, and not negative
   */
  protected boolean shouldExpireWithin(@Nullable Duration ttl) {
    return ttl != null && !ttl.isZero() && !ttl.isNegative();
  }

}
