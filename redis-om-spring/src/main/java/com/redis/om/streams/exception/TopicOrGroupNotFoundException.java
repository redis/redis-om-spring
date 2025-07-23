package com.redis.om.streams.exception;

/**
 * Exception thrown when either a Redis Stream topic or a consumer group cannot be found.
 * This typically occurs when attempting to perform operations on a non-existent topic
 * or when trying to use a consumer group that doesn't exist.
 */
public class TopicOrGroupNotFoundException extends TopicException {

  /**
   * Constructs a new TopicOrGroupNotFoundException with the specified detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
   */
  public TopicOrGroupNotFoundException(String message) {
    super(message);
  }
}
