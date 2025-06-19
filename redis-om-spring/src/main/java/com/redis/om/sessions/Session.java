/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public interface Session {
  /**
   * Get the id of the session
   * 
   * @return the sessions id
   */
  String getId();

  /**
   * Change the session id
   * 
   * @return the new session id
   */
  String changeSessionId();

  /**
   * Change the session id
   * 
   * @param sessionId the new session id
   * @return the new session id
   */
  String changeSessionId(String sessionId);

  /**
   * Get the value of an attribute
   * 
   * @param attribute the name of the attribute
   * @return the value of the attribute
   */
  <T> Optional<T> getAttribute(String attribute);

  /**
   * Get all the attribute names
   * 
   * @return the set of attribute names
   */
  Set<String> getAttributeNames();

  /**
   * Set an attribute
   * 
   * @param attributeName  the name of the attribute
   * @param attributeValue the value of the attribute
   * @return the session
   */
  <T> Long setAttribute(String attributeName, T attributeValue);

  /**
   * Remove an attribute
   * 
   * @param attributeName the name of the attribute
   */
  void removeAttribute(String attributeName);

  /**
   * Get the creation time of the session
   * 
   * @return the creation time
   */
  Instant getCreationTime();

  /**
   * Set the last accessed time of the session
   * 
   * @param lastAccessedTime the last accessed time
   */
  void setLastAccessedTime(Instant lastAccessedTime);

  /**
   * Get the last accessed time of the session
   * 
   * @return the last accessed time
   */
  Instant getLastAccessedTime();

  /**
   * Set the max inactive interval of the session
   * 
   * @param interval the max inactive interval
   */
  void setMaxInactiveInterval(Duration interval);

  /**
   * Get the max inactive interval of the session
   * 
   * @return the max inactive interval
   */
  Optional<Duration> getMaxInactiveInterval();

  /**
   * Check if the session is expired
   * 
   * @return true if the session is expired, false otherwise
   */
  boolean isExpired();

  /**
   * Save the session
   * 
   * @return the size of the session
   */
  Long save();

  /**
   * Get the size of the session
   * 
   * @return the size of the session
   */
  long getSize();

  /**
   * Get the last modified time of the session
   * 
   * @return the last modified time of the session
   */
  long getLastModifiedTime();
}
