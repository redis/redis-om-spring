package com.redis.om.streams.exception;

/**
 * Exception thrown when a specified Redis Stream topic cannot be found.
 * This typically occurs when attempting to perform operations on a non-existent topic.
 */
public class TopicNotFoundException extends TopicException {
  /**
   * Constructs a new TopicNotFoundException with the specified error message.
   *
   * @param errorMessage the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
   */
  public TopicNotFoundException(String errorMessage) {
    super(errorMessage);
  }
}
