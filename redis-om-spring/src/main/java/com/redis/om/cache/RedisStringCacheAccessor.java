package com.redis.om.cache;

import java.time.Duration;

import org.springframework.lang.Nullable;

import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.om.cache.common.RedisStringMapper;

import io.lettuce.core.GetExArgs;
import io.lettuce.core.SetArgs;

/**
 * Implementation of {@link AbstractRedisCacheAccessor} that uses Redis String data structures
 * for caching. This accessor uses a {@link RedisStringMapper} to convert between objects and
 * their string representation in Redis.
 */
public class RedisStringCacheAccessor extends AbstractRedisCacheAccessor {

  private final RedisStringMapper mapper;

  /**
   * @param connection must not be {@literal null}.
   * @param mapper     the mapper used to convert between objects and their string representation
   */
  public RedisStringCacheAccessor(StatefulRedisModulesConnection<byte[], byte[]> connection, RedisStringMapper mapper) {
    super(connection);
    this.mapper = mapper;
  }

  @Override
  public Object get(byte[] key, @Nullable Duration ttl) {
    if (shouldExpireWithin(ttl)) {
      return mapper.fromString(connection.sync().getex(key, GetExArgs.Builder.ex(ttl)));
    }
    return mapper.fromString(connection.sync().get(key));
  }

  @Override
  public void put(byte[] key, Object value, Duration ttl) {
    doPut(key, value, ttl);
  }

  private void doPut(byte[] key, Object value, Duration ttl) {
    if (shouldExpireWithin(ttl)) {
      connection.sync().psetex(key, ttl.toMillis(), mapper.toString(value));
    } else {
      connection.sync().set(key, mapper.toString(value));
    }
  }

  @Override
  public Object putIfAbsent(byte[] key, Object value, Duration ttl) {
    SetArgs args = SetArgs.Builder.nx();
    if (shouldExpireWithin(ttl)) {
      args.ex(ttl);
    }
    boolean put = "OK".equalsIgnoreCase(connection.sync().set(key, mapper.toString(value), args));
    if (put) {
      return null;
    }
    return mapper.fromString(connection.sync().get(key));
  }

}
