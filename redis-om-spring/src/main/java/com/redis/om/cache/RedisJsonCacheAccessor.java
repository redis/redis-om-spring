package com.redis.om.cache;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.om.cache.common.RedisStringMapper;

import io.lettuce.core.json.JsonPath;
import io.lettuce.core.json.JsonValue;

/**
 * Implementation of {@link AbstractRedisCacheAccessor} that uses Redis JSON to store and retrieve cache values.
 * This accessor uses a {@link RedisStringMapper} to convert objects to and from JSON strings.
 */
public class RedisJsonCacheAccessor extends AbstractRedisCacheAccessor {

  private final RedisStringMapper mapper;

  /**
   * @param connection must not be {@literal null}.
   * @param mapper     the mapper used to convert objects to and from JSON strings
   */
  public RedisJsonCacheAccessor(StatefulRedisModulesConnection<byte[], byte[]> connection, RedisStringMapper mapper) {
    super(connection);
    this.mapper = mapper;
  }

  @Override
  public Object get(byte[] key, Duration ttl) {
    List<JsonValue> jsonValues = null;
    try {
      jsonValues = connection.sync().jsonGet(key);
    } catch (NullPointerException e) {
      // Workaround for Lettuce 6.5 bug:
      // https://github.com/redis/lettuce/commit/341cdadc987e2866432dc6700b34b0f869134ae6
      // TODO: Remove with Lettuce 6.5.6+
    }
    if (CollectionUtils.isEmpty(jsonValues)) {
      return null;
    }
    JsonValue jsonValue = jsonValues.get(0);
    if (jsonValue == null) {
      return null;
    }
    ByteBuffer byteBuffer = jsonValue.asByteBuffer();
    if (byteBuffer == null) {
      return null;
    }
    Object value = mapper.fromString(byteBuffer.array());
    if (shouldExpireWithin(ttl)) {
      connection.sync().pexpire(key, ttl.toMillis());
    }
    return value;
  }

  @Override
  public void put(byte[] key, Object value, Duration ttl) {
    doPut(key, value, ttl);
  }

  private void doPut(byte[] key, Object value, Duration ttl) {
    connection.sync().jsonSet(key, JsonPath.ROOT_PATH, connection.sync().getJsonParser().createJsonValue(ByteBuffer
        .wrap(mapper.toString(value))));
    if (shouldExpireWithin(ttl)) {
      connection.sync().pexpire(key, ttl.toMillis());
    }
  }

  @Override
  public Object putIfAbsent(byte[] key, Object value, Duration ttl) {
    if (exists(key)) {
      return get(key, null);
    }
    doPut(key, value, ttl);
    return null;
  }

}
