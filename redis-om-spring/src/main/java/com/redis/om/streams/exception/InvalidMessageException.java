package com.redis.om.streams.exception;

/**
 * Exception thrown when an invalid message is encountered in Redis Streams operations.
 * This typically occurs when a message format is incorrect or cannot be processed.
 */
public class InvalidMessageException extends TopicException {
  /**
   * Constructs a new InvalidMessageException with the specified detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
   */
  public InvalidMessageException(String message) {
    super(message);
  }
}
