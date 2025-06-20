package com.redis.om.streams;

import java.util.Map;

import com.redis.om.streams.exception.InvalidMessageException;
import com.redis.om.streams.exception.ProducerTimeoutException;
import com.redis.om.streams.exception.TopicNotFoundException;

/**
 * Interface for producing messages to Redis Streams.
 * Implementations of this interface handle the details of sending messages to Redis Streams topics.
 */
public interface Producer {

  /**
   * Produces a message to a Redis Stream topic.
   *
   * @param message The message to produce, represented as key-value pairs
   * @return The unique identifier assigned to the produced message
   * @throws TopicNotFoundException   If the specified topic does not exist
   * @throws InvalidMessageException  If the message format is invalid
   * @throws ProducerTimeoutException If the operation times out
   */
  public TopicEntryId produce(Map<String, String> message) throws TopicNotFoundException, InvalidMessageException,
      ProducerTimeoutException;
}
