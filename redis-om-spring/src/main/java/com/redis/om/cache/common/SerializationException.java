package com.redis.om.cache.common;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception thrown when an error occurs during serialization or deserialization operations.
 * This exception is typically thrown by implementations of {@link RedisStringMapper} and
 * {@link RedisHashMapper} when they encounter issues converting between Java objects and
 * Redis data structures.
 */
public class SerializationException extends NestedRuntimeException {

  /**
   * Constructs a new {@link SerializationException} instance.
   *
   * @param msg the detail message describing the error
   */
  public SerializationException(String msg) {
    super(msg);
  }

  /**
   * Constructs a new {@link SerializationException} instance.
   *
   * @param msg   the detail message describing the error
   * @param cause the nested exception that caused this exception
   */
  public SerializationException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
