package com.redis.om.cache.common.mapping;

import org.springframework.lang.Nullable;

import com.redis.om.cache.common.RedisStringMapper;

/**
 * Implementation of {@link RedisStringMapper} that handles byte arrays.
 * This mapper acts as a pass-through for byte array data, returning the byte arrays directly
 * without any transformation in both directions.
 */
public class ByteArrayMapper implements RedisStringMapper {

  /**
   * Singleton instance of ByteArrayMapper for convenient access.
   */
  public static final ByteArrayMapper INSTANCE = new ByteArrayMapper();

  @Nullable
  @Override
  public byte[] toString(@Nullable Object value) {
    return (byte[]) value;
  }

  @Nullable
  @Override
  public byte[] fromString(@Nullable byte[] bytes) {
    return bytes;
  }
}
