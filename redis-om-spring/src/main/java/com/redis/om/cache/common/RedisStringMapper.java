package com.redis.om.cache.common;

/**
 * Interface for mapping between Java objects and Redis string structures.
 * Implementations of this interface handle the conversion of Java objects to Redis string values
 * and vice versa.
 */
public interface RedisStringMapper {

  /**
   * Converts a Java object to a Redis string representation.
   *
   * @param value the Java object to convert
   * @return a byte array representing the Redis string
   */
  byte[] toString(Object value);

  /**
   * Converts a Redis string representation back to a Java object.
   *
   * @param bytes the Redis string as a byte array
   * @return the reconstructed Java object
   */
  Object fromString(byte[] bytes);

}
