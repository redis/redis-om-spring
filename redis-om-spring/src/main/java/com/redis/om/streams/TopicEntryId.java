package com.redis.om.streams;

import java.nio.ByteBuffer;

import com.redis.om.streams.utils.Util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import redis.clients.jedis.StreamEntryID;

/**
 * Represents a unique identifier for a topic entry in Redis Streams.
 * The ID consists of three components: time, sequence, and streamId.
 * This class provides methods to create, manipulate, and convert these IDs.
 */
@EqualsAndHashCode
public class TopicEntryId {
  /**
   * The timestamp component of the ID.
   */
  @Getter
  private long time;

  /**
   * The sequence number component of the ID.
   */
  @Getter
  private long sequence;

  /**
   * The stream identifier component of the ID.
   */
  @Getter
  private long streamId;

  /**
   * A string of zeros used for padding long values in string representation.
   */
  final private String PADDED_LONG = "0000000000000000000";

  /**
   * Represents the minimum possible TopicEntryId with all components set to 0.
   */
  static final public TopicEntryId MIN_ID = new TopicEntryId(0, 0, 0);

  /**
   * Represents the maximum possible TopicEntryId with all components set to a large value.
   */
  static final public TopicEntryId MAX_ID = new TopicEntryId(5999999999999L, 5999999999999L, 5999999999999L);

  /**
   * Constructs a TopicEntryId with the specified time, sequence, and streamId.
   *
   * @param time     The timestamp component of the ID
   * @param sequence The sequence number component of the ID
   * @param streamId The stream identifier component of the ID
   */
  public TopicEntryId(long time, long sequence, long streamId) {
    this.time = time;
    this.sequence = sequence;
    this.streamId = streamId;
  }

  /**
   * Constructs a TopicEntryId from a StreamEntryID and a stream identifier.
   *
   * @param streamEntryId The Redis StreamEntryID containing time and sequence components
   * @param streamId      The stream identifier component of the ID
   */
  public TopicEntryId(StreamEntryID streamEntryId, long streamId) {
    this.time = streamEntryId.getTime();
    this.sequence = streamEntryId.getSequence();
    this.streamId = streamId;
  }

  /**
   * Constructs a TopicEntryId from a StreamEntryID and a stream name.
   * The stream identifier is derived from the stream name.
   *
   * @param streamEntryId The Redis StreamEntryID containing time and sequence components
   * @param streamName    The name of the stream, used to derive the stream identifier
   */
  public TopicEntryId(StreamEntryID streamEntryId, String streamName) {
    this.time = streamEntryId.getTime();
    this.sequence = streamEntryId.getSequence();
    this.streamId = Util.streamIdFromStreamName(streamName);
  }

  /**
   * Constructs a TopicEntryId from its string representation.
   * The string format is: TIME-SEQUENCE-STREAMID
   *
   * @param id The string representation of the TopicEntryId
   */
  public TopicEntryId(String id) {
    String[] split = id.split("-");
    this.time = Long.parseLong(split[0]);
    this.sequence = Long.parseLong(split[1]);
    this.streamId = Long.parseLong(split[2]);
  }

  /**
   * Constructs a TopicEntryId from a byte array.
   * 
   * @param bytes The byte array containing the TopicEntryId data
   */
  public TopicEntryId(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
  }

  /**
   * Returns a string representation of this TopicEntryId.
   * The format is: TIME-SEQUENCE-STREAMID
   *
   * @return The string representation of this TopicEntryId
   */
  public String toString() {
    return String.valueOf(time) + "-" + String.valueOf(sequence) + "-" + String.valueOf(streamId);
  }

  /**
   * Returns a padded string representation of this TopicEntryId.
   * Each component is padded with leading zeros to ensure consistent string length.
   *
   * @return The padded string representation of this TopicEntryId
   */
  public String toPaddedString() {
    return getPaddedLong(time) + "-" + getPaddedLong(sequence) + "-" + getPaddedLong(streamId);
  }

  /**
   * Pads a long value with leading zeros to ensure consistent string length.
   *
   * @param value The long value to pad
   * @return The padded string representation of the long value
   */
  private String getPaddedLong(long value) {
    String longStringValue = String.valueOf(value);
    int zeros = PADDED_LONG.length() - longStringValue.length();
    return PADDED_LONG.substring(0, zeros) + longStringValue;
  }

  /**
   * Converts this TopicEntryId to a Redis StreamEntryID.
   * Only the time and sequence components are used; the streamId is not included.
   *
   * @return A new StreamEntryID with the time and sequence from this TopicEntryId
   */
  public StreamEntryID getStreamEntryId() {
    return new StreamEntryID(time, sequence);
  }
}
