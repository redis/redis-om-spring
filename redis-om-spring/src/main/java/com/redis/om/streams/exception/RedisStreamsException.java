package com.redis.om.streams.exception;

/**
 * Base exception class for Redis Streams related exceptions.
 * This exception is thrown for general errors that occur during Redis Streams operations.
 * 
 * TODO: RedisStreamsException should inherit from Exception.
 */
public class RedisStreamsException extends RuntimeException {
  /**
   * Constructs a new RedisStreamsException with the specified detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
   */
  public RedisStreamsException(String message) {
    super(message);
  }
}
