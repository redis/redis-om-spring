package com.redis.om.cache.common.convert;

import org.springframework.data.convert.TypeMapper;

import com.redis.om.cache.common.convert.Bucket.BucketPropertyPath;

/**
 * Redis-specific {@link TypeMapper} exposing that {@link BucketPropertyPath}s might contain a type key.
 *
 */
public interface RedisTypeMapper extends TypeMapper<BucketPropertyPath> {

  /**
   * Returns whether the given {@code key} is the type key.
   *
   * @param key the key to check
   * @return {@literal true} if the given {@code key} is the type key.
   */
  boolean isTypeKey(String key);
}
