package com.redis.om.cache.common.convert;

import java.util.Map;

import org.springframework.data.annotation.Reference;
import org.springframework.lang.Nullable;

/**
 * {@link ReferenceResolver} retrieves Objects marked with {@link Reference} from Redis.
 *
 */
public interface ReferenceResolver {

  /**
   * @param id       must not be {@literal null}.
   * @param keyspace must not be {@literal null}.
   * @return {@literal null} if referenced object does not exist.
   */
  @Nullable
  Map<byte[], byte[]> resolveReference(Object id, String keyspace);
}
