package com.redis.om.streams.exception;

/**
 * Exception thrown when an invalid topic is specified in Redis Streams operations.
 * This typically occurs when a topic name is malformed or does not meet the required criteria.
 */
public class InvalidTopicException extends TopicException {
  /**
   * Constructs a new InvalidTopicException with the specified detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
   */
  public InvalidTopicException(String message) {
    super(message);
  }
}
