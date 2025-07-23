package com.redis.om.cache;

import java.time.Duration;

import org.springframework.lang.Nullable;

import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.om.cache.common.RedisHashMapper;

/**
 * Implementation of {@link CacheAccessor} that stores objects as Redis hashes.
 * Uses a {@link RedisHashMapper} to convert objects to and from Redis hash entries.
 */
public class RedisHashCacheAccessor extends AbstractRedisCacheAccessor {

  private final RedisHashMapper mapper;

  /**
   * Creates a new {@link RedisHashCacheAccessor} with the given connection and mapper.
   *
   * @param connection must not be {@literal null}.
   * @param mapper     the mapper used to convert objects to and from Redis hash entries, must not be {@literal null}.
   */
  public RedisHashCacheAccessor(StatefulRedisModulesConnection<byte[], byte[]> connection, RedisHashMapper mapper) {
    super(connection);
    this.mapper = mapper;
  }

  @Override
  protected Object get(byte[] key, Duration ttl) {
    Object value = mapper.fromHash(connection.sync().hgetall(key));
    if (shouldExpireWithin(ttl)) {
      connection.sync().pexpire(key, ttl.toMillis());
    }
    return value;
  }

  @Override
  protected void put(byte[] key, Object value, @Nullable Duration ttl) {
    doPut(key, value, ttl);
  }

  private void doPut(byte[] key, Object value, Duration ttl) {
    connection.sync().hset(key, mapper.toHash(value));
    if (shouldExpireWithin(ttl)) {
      connection.sync().pexpire(key, ttl.toMillis());
    }
  }

  @Override
  protected Object putIfAbsent(byte[] key, Object value, Duration ttl) {
    if (exists(key)) {
      return get(key, null);
    }
    doPut(key, value, ttl);
    return null;
  }

}
