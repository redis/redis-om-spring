package com.redis.om.streams.command.serial;

import com.redis.om.streams.TopicEntryId;

import lombok.Data;
import lombok.NonNull;

/**
 * A query object for retrieving pending entries from a Redis Stream consumer group.
 * This class defines the parameters for filtering pending entries based on ID range,
 * idle time, and count.
 */
@Data
public class PendingEntryQuery {

  /**
   * The starting ID (inclusive) for the range of pending entries to retrieve.
   */
  @NonNull
  private TopicEntryId startId;

  /**
   * The ending ID (exclusive) for the range of pending entries to retrieve.
   */
  @NonNull
  private TopicEntryId endId;

  /**
   * The minimum idle time in milliseconds for pending entries to be included in the result.
   */
  private int minIdleTimeMilliSeconds;

  /**
   * The maximum number of pending entries to retrieve.
   */
  private int count;

  /**
   * Constructs a new PendingEntryQuery with default values.
   * By default, it retrieves up to 1000 entries across the entire ID range
   * with no minimum idle time.
   */
  public PendingEntryQuery() {
    this.startId = TopicEntryId.MIN_ID;
    this.endId = TopicEntryId.MAX_ID;
    this.minIdleTimeMilliSeconds = 0;
    this.count = 1000;
  }
}
