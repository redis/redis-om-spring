package com.redis.om.streams.handler;

/**
 * Interface for Redis Streams message handlers.
 * 
 * <p>This interface defines the contract for components that process messages
 * from Redis Streams. Implementations of this interface are responsible for
 * consuming messages from a Redis Stream and processing them according to
 * application-specific logic.</p>
 * 
 * <p>The interface is designed to be simple, with a single method that handles
 * the message processing logic.</p>
 */
public interface RedisStreamsHandler {

  /**
   * Processes messages from a Redis Stream.
   * 
   * <p>This method is called to process messages from a Redis Stream. The implementation
   * should handle consuming messages from the stream and applying the appropriate
   * business logic to process them.</p>
   * 
   * @return true if a message was successfully processed, false otherwise
   */
  boolean process();

}
