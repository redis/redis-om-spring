package com.redis.om.streams.command.singleclusterpel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.redis.om.streams.ConsumerGroupStatus;
import com.redis.om.streams.PendingEntry;
import com.redis.om.streams.TopicEntryId;
import com.redis.om.streams.command.serial.PendingEntryQuery;
import com.redis.om.streams.command.serial.SerialTopicConfig;
import com.redis.om.streams.command.serial.TopicManager;
import com.redis.om.streams.exception.InvalidTopicException;
import com.redis.om.streams.exception.TopicOrGroupNotFoundException;
import com.redis.om.streams.utils.Util;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XPendingParams;
import redis.clients.jedis.resps.StreamGroupInfo;
import redis.clients.jedis.resps.StreamPendingEntry;

/**
 * A specialized implementation of TopicManager that manages topics in a single cluster with
 * Pending Entry List (PEL) functionality. This class provides methods for managing consumer groups,
 * retrieving pending entries, and handling stream operations in a single Redis cluster environment.
 */
public class SingleClusterPelTopicManager extends TopicManager {
  /**
   * Constructs a new SingleClusterPelTopicManager with the specified connection and configuration.
   *
   * @param connection The JedisPooled connection to the Redis server
   * @param config     The configuration for the serial topic
   */
  public SingleClusterPelTopicManager(JedisPooled connection, SerialTopicConfig config) {
    super(connection, config);
  }

  /**
   * Creates a new topic with the specified configuration and returns a manager for it.
   *
   * @param connection The JedisPooled connection to the Redis server
   * @param config     The configuration for the serial topic
   * @return A new SingleClusterPelTopicManager instance for the created topic
   * @throws InvalidTopicException If the topic configuration is invalid
   */
  public static SingleClusterPelTopicManager createTopic(JedisPooled connection, SerialTopicConfig config)
      throws InvalidTopicException {
    TopicManager.createTopic(connection, config);
    return new SingleClusterPelTopicManager(connection, config);
  }

  /**
   * Retrieves the status of a consumer group including topic size, lag, and pending entry count.
   *
   * @param groupName The name of the consumer group
   * @return A ConsumerGroupStatus object containing status information
   */
  public ConsumerGroupStatus getConsumerGroupStatus(String groupName) {
    long topicSize = getTopicSize();
    long lag = 0;
    try {
      lag = getConsumerGroupLag(groupName);
    } catch (TopicOrGroupNotFoundException e) {
      lag = topicSize;
    }

    ConsumerGroupStatus status = new ConsumerGroupStatus(config.getTopicName(), groupName, getPendingEntryCount(
        groupName), topicSize, lag);

    return status;
  }

  /**
   * Gets the count of pending entries for a consumer group.
   *
   * @param groupName The name of the consumer group
   * @return The number of pending entries for the specified consumer group
   */
  public long getPendingEntryCount(String groupName) {
    return getSingleDBPendingEntryCount(groupName);
  }

  /**
   * Retrieves pending entries for a consumer group using default query parameters.
   * 
   * @param groupName The name of the consumer group
   * @return A list of pending entries for the specified consumer group
   */
  public List<PendingEntry> getPendingEntries(String groupName) {
    PendingEntryQuery query = new PendingEntryQuery();
    return getSingleDBPendingEntries(groupName, query.getStartId(), query.getEndId(), query
        .getMinIdleTimeMilliSeconds(), query.getCount());
  }

  /**
   * Retrieves pending entries for a consumer group within the specified ID range and count limit.
   * Uses a default idle time of 0 milliseconds.
   *
   * @param groupName The name of the consumer group
   * @param startId   The starting ID for the range of entries to retrieve
   * @param endId     The ending ID for the range of entries to retrieve
   * @param count     The maximum number of entries to retrieve
   * @return A list of pending entries for the specified consumer group within the given parameters
   */
  public List<PendingEntry> getPendingEntries(String groupName, TopicEntryId startId, TopicEntryId endId, int count) {
    int minIdleTimeMilliSeconds = 0;
    return getSingleDBPendingEntries(groupName, startId, endId, minIdleTimeMilliSeconds, count);
  }

  /**
   * Retrieves pending entries for a consumer group within the specified ID range, idle time, and count limit.
   *
   * @param groupName               The name of the consumer group
   * @param startId                 The starting ID for the range of entries to retrieve
   * @param endId                   The ending ID for the range of entries to retrieve
   * @param minIdleTimeMilliSeconds The minimum idle time in milliseconds for entries to be included
   * @param count                   The maximum number of entries to retrieve
   * @return A list of pending entries for the specified consumer group within the given parameters
   */
  public List<PendingEntry> getPendingEntries(String groupName, TopicEntryId startId, TopicEntryId endId,
      int minIdleTimeMilliSeconds, int count) {
    return getSingleDBPendingEntries(groupName, startId, endId, minIdleTimeMilliSeconds, count);
  }

  /**
   * Retrieves pending entries for a consumer group using the parameters specified in the query object.
   *
   * @param groupName The name of the consumer group
   * @param query     The query object containing parameters for retrieving pending entries
   * @return A list of pending entries for the specified consumer group based on the query parameters
   */
  public List<PendingEntry> getPendingEntries(String groupName, PendingEntryQuery query) {
    return getSingleDBPendingEntries(groupName, query.getStartId(), query.getEndId(), query
        .getMinIdleTimeMilliSeconds(), query.getCount());
  }

  /**
   * Returns the total number of pending entries for the given consumer group in a single database PEL scenario.
   * This method calls out to every stream serving the specified group to calculate the total count.
   * 
   * @param groupName The name of the consumer group
   * @return The total number of pending entries for the specified consumer group across all streams
   */
  public long getSingleDBPendingEntryCount(String groupName) {
    long count = 0;
    long streamCheckCountForGroup = 0;
    long streamNameLastDigits = 0;
    for (String streamName : getStreamNames()) { //only streams for this topic will be returned?
      try {
        streamNameLastDigits = Long.parseLong(streamName.split(":")[4]);
        Optional<StreamGroupInfo> info = connection.xinfoGroups(streamName).stream().filter(group -> group.getName()
            .equals(groupName)).findFirst();
        if (info.isPresent()) {
          count += info.get().getPending();
        }

        streamCheckCountForGroup++;
      } catch (redis.clients.jedis.exceptions.JedisDataException jde) {
        //This happens whenever a group has not caught up to total # of possible stream keys
        // let's check to see if we are at or beyond the currentStream:
        String groupConfigKeyName = config.getConsumerGroupConfigKey(groupName);
        String currentStreamFieldName = config.getGroupCurrentStreamField();
        String currentStreamForGroup = connection.hget(groupConfigKeyName, currentStreamFieldName);
        // sample stream name: __rsj:topic:stream:Topic-SINGLEDBPEL-1618:0
        // there are 4 segments to the name divided by : tokens
        if ((currentStreamForGroup.equals(streamName)) || (streamNameLastDigits < streamCheckCountForGroup)) {
          System.out.println(
              "streamNameLastDigits:  " + streamNameLastDigits + "  streamCheckCountForGroup: " + streamCheckCountForGroup + "  groupConfigKeyName:  " + groupConfigKeyName + "  currentStreamForGroup:  " + currentStreamForGroup);
          throw jde;
        } else {
          //we can assume we are beyond the Streams currently being consumed by our group
          //do nothing but break out of the loop
          break;
        }
      }
    }
    return count;
  }

  /**
   * Retrieves pending entries for a consumer group in a single database PEL scenario.
   * In this scenario, there is no __PEL__ key created, and pending entries are managed locally.
   * 
   * @param groupName               The name of the consumer group
   * @param startId                 The starting ID for the range of entries to retrieve
   * @param endId                   The ending ID for the range of entries to retrieve
   * @param minIdleTimeMilliSeconds The minimum idle time in milliseconds for entries to be included
   * @param count                   The maximum number of entries to retrieve
   * @return A list of pending entries for the specified consumer group within the given parameters
   */
  public List<PendingEntry> getSingleDBPendingEntries(String groupName, TopicEntryId startId, TopicEntryId endId,
      int minIdleTimeMilliSeconds, int count) {
    // Don't return more than 1000 messages.
    if (count > 1000) {
      count = 1000;
    } else if (count <= 0) {
      return Collections.emptyList();
    }

    if (getTopicSize() == 0) {
      return Collections.emptyList();
    }

    // We need the boundary IDs as Doubles so that we can use them for comparisons later.
    Double startIdAsDouble = idAsDouble(startId.getStreamEntryId());
    Double endIdAsDouble = idAsDouble(endId.getStreamEntryId());

    // We need a flag to mark the iteration as complete.
    boolean complete = false;

    // Get the server time so that we can calculate pending entry idle time.
    long serverTimeMs = Util.getServerTimeMs(connection);

    // Now get up to _count_ entries from the streams:
    List<PendingEntry> pendingEntries = new ArrayList<>();

    //ot working here to use standard stream API:
    // Return the first stream that may contain pending entries.
    String streamName = "";
    streamName = getStreamForID(startId);
    // ot : this implementation relies on count to optimize the check for pending entries within a single stream:
    // it also checks to ensure this group is consuming from the nextStream
    // the XPendingParams use MAXIMUM and MINIMUM IDs to check for Pending entries
    // however:
    // the initial stream in the loop will be as young as the startId demands
    // the last stream in the loop will be as young as the count and/or endId demand
    String groupConfigKeyName = config.getConsumerGroupConfigKey(groupName);
    String currentStreamFieldName = config.getGroupCurrentStreamField();
    String currentStreamForGroup = connection.hget(groupConfigKeyName, currentStreamFieldName);
    long streamCheckCountForGroup = 0;
    long streamNameLastDigits = 0;
    while (streamName != null && pendingEntries.size() < count && !complete) {
      //System.out.println("DEBUG:\nminIdleTimeMilliSeconds arg == "+minIdleTimeMilliSeconds+
      //        "\n   also: if(streamName != null && pendingEntries.size() < count && !complete)  IS TRUE");
      List<StreamPendingEntry> streamEntries = null;
      try {
        XPendingParams params = new XPendingParams(StreamEntryID.MINIMUM_ID, StreamEntryID.MAXIMUM_ID, count);
        streamEntries = connection.xpending(streamName, groupName, params);
      } catch (Throwable t) {
        if ((currentStreamForGroup.equals(streamName)) || (streamNameLastDigits < streamCheckCountForGroup)) {
          throw t;
        } else {
          break;
        }
      }
      for (StreamPendingEntry entry : streamEntries) {
        if (idAsDouble(entry.getID()) > endIdAsDouble) {
          complete = true;
          //System.out.println("DEBUG:\nentry.getIdleTime(): "+entry.getIdleTime()+"\n"+
          //        "minIdleTimeMilliSeconds arg == "+minIdleTimeMilliSeconds+"\n   also: if(idAsDouble(entry.getID()) > endIdAsDouble)  IS TRUE");
          break;
        }
        // Add the pending entries one at a time until we reach the endID.
        // Filter by idle time if a min idle time is provided
        if (entry.getIdleTime() >= minIdleTimeMilliSeconds) {
          //if (entryIdleTimeMilliSeconds(entry, serverTimeMs) >= minIdleTimeMilliSeconds) {
          //Thought : do  we need to / can we construct a PendingEntry from a StreamPendingEntry??
          String consumer = entry.getConsumerName();
          String deliveryTime = String.valueOf(entry.getDeliveredTimes());
          Long idleTimeMs = entry.getIdleTime();//serverTimeMs - Long.valueOf(deliveryTime);
          PendingEntry nextPendingEntry = new PendingEntry(entry.getID(), config.getTopicName(), streamName, groupName,
              consumer, idleTimeMs, 1L);
          pendingEntries.add(nextPendingEntry);
          //System.out.println("OTMay3rd: PENDING ENTRIES INSIDE LOOP\n"+pendingEntries);
        }
      }
      streamName = getNextStream(streamName, endId);
      //end of  if((currentStreamForGroup.equals(streamName))||(streamNameLastDigits < streamCheckCountForGroup)){
      if ((pendingEntries.size() >= count) || (null == streamName)) {
        break; // stop the loop - we don't want to count anymore PELs
      }
      streamNameLastDigits = Long.parseLong(streamName.split(":")[4]);
      streamCheckCountForGroup++;
    }//loop continues...
    return pendingEntries;
  }

  /**
   * Retrieves pending entries for a consumer group using the parameters specified in the query object.
   * This is a convenience method that delegates to the more detailed getSingleDBPendingEntries method.
   *
   * @param consumerGroupName The name of the consumer group
   * @param query             The query object containing parameters for retrieving pending entries
   * @return A list of pending entries for the specified consumer group based on the query parameters
   */
  public List<PendingEntry> getSingleDBPendingEntries(String consumerGroupName, PendingEntryQuery query) {
    return getSingleDBPendingEntries(consumerGroupName, query.getStartId(), query.getEndId(), query
        .getMinIdleTimeMilliSeconds(), query.getCount());
  }
}
