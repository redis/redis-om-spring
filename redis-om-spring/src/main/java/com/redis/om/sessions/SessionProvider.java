/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redis.om.sessions.filtering.Filter;

import io.lettuce.core.KeyValue;

public interface SessionProvider<T extends Session> extends AutoCloseable {
  /**
   * Create a new Session
   * 
   * @param sessionId   the id of the session
   * @param sessionData the data for the session
   * @return the new session
   */
  T createSession(String sessionId, Map<String, Object> sessionData);

  /**
   * Retrieves a session by its id
   * 
   * @param id the id of the session to retrive
   * @return the session
   */
  T findSessionById(String id);

  /**
   * Deletes a session with a given id
   * 
   * @param id the id of the session to delete
   */
  void deleteSessionById(String id);

  /**
   * Finds a session with an exact string match
   * 
   * @param fieldName
   * @param fieldValue
   * @return sessions matching the exact string match.
   * @throws Exception
   */
  Map<String, T> findSessionsByExactMatch(String fieldName, String fieldValue) throws Exception;

  /**
   * Bootstraps the session provider, Creating the necessary indexes and metrics tracking data structures for the
   * provider
   */
  void bootstrap();

  /**
   * Deletes the index associated with the session provider
   * 
   * @param dropAssociatedRecords whether or not to delete all the sessions currently mapped by the index.
   */
  void dropIndex(boolean dropAssociatedRecords);

  /**
   * Returns the top-k largest sessions
   * 
   * @param topK the number of largest sessions to return
   * @return the session Ids along with their sizes
   */
  Map<String, Long> largestSessions(int topK);

  /**
   * Returns the sessions that have been most heavily accessed.
   * 
   * @return the session Ids along with the number of times they have been accessed.
   */
  List<KeyValue<String, Long>> mostAccessedSessions();

  /**
   * Finds the sessions that match the provided filter
   * 
   * @param filter the filter to use to search for sessions
   * @param limit  the number of sessions to return
   * @return session Ids along with the sessions that match the filter
   */
  Map<String, T> findSessions(Filter filter, int limit);

  /**
   * Finds sessions that match the provided filter, ordering the results by the provided field
   * 
   * @param filter    the filter to use to search for sessions
   * @param sortBy    the field to order the results by
   * @param ascending whether to order the results in ascending or descending order
   * @param limit     the number of sessions to return
   * @return session Ids along with the sessions that match the filter
   */
  Map<String, T> findSessions(Filter filter, String sortBy, boolean ascending, int limit);

  /**
   * Deletes sessions that match the provided filter
   * 
   * @param filter the filter to use to search for sessions
   * @param limit  the number of sessions to delete
   * @return the session Ids of the sessions that were deleted
   */
  Set<String> deleteSessions(Filter filter, int limit);

  /**
   * Updates the sessions that match the provided filter by setting the provided field to the provided value
   * 
   * @param filter the filter to use to search for sessions
   * @param limit  the number of sessions to update
   * @param field  the name of the field to update.
   * @param value  the value to set the field to.
   * @return the session Ids of the sessions that were updated.
   */
  Set<String> updateSessions(Filter filter, int limit, String field, Object value);

  /**
   * Retrieves the size of the session at the provided quantiles
   * 
   * @param quantiles a set of numbers between 0 and 1 representing the quantiles to retrieve
   * @return the session sizes at the provided quantiles.
   */
  List<Double> sessionSizeQuantiles(double[] quantiles);

  /**
   * Retrieves the approximate number of unique sessions
   * 
   * @return the approximate number of unique sessions
   */
  Long uniqueSessions();

  /**
   * Adds a recording of the session size to the redis session provider, for internal use only, to help with metrics
   * tracking
   * 
   * @param sessionSize the size of the session to record
   */
  void addSessionSize(Long sessionSize);

  /**
   * Retrieves the session size distribution
   * 
   * @return returns the Local cache statistics.
   */
  LocalCacheStatistics getLocalCacheStatistics();

}
