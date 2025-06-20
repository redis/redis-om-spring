package com.redis.om.streams.exception;

/**
 * Exception thrown when a producer operation times out in Redis Streams.
 * This typically occurs when a message production operation takes longer than the configured timeout period.
 */
public class ProducerTimeoutException extends TopicException {
  /**
   * Constructs a new ProducerTimeoutException with the specified detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
   */
  public ProducerTimeoutException(String message) {
    super(message);
  }
}
