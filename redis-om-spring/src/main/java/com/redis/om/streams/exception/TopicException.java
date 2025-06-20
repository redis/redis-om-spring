package com.redis.om.streams.exception;

/**
 * Base exception class for Redis Streams topic-related exceptions.
 * This class serves as the parent for more specific topic-related exceptions
 * and is thrown when an error occurs related to Redis Stream topics.
 */
public class TopicException extends Exception {

  /**
   * Constructs a new TopicException with the specified detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
   */
  public TopicException(String message) {
    super(message);
  }
}
