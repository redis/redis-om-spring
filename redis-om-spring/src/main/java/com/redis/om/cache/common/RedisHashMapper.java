package com.redis.om.cache.common;

import java.util.Map;

/**
 * Interface for mapping between Java objects and Redis hash structures.
 * Implementations of this interface handle the conversion of Java objects to Redis hash maps
 * and vice versa.
 */
public interface RedisHashMapper {

  /**
   * Converts a Java object to a Redis hash representation.
   *
   * @param value the Java object to convert
   * @return a map of byte arrays representing the Redis hash
   */
  Map<byte[], byte[]> toHash(Object value);

  /**
   * Converts a Redis hash representation back to a Java object.
   *
   * @param hash the Redis hash as a map of byte arrays
   * @return the reconstructed Java object
   */
  Object fromHash(Map<byte[], byte[]> hash);

}
