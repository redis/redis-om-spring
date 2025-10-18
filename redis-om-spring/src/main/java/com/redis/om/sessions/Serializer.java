/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

public interface Serializer {
  /**
   * Serialize an object to a byte array
   * 
   * @param object the object to serialize
   * @return the byte arrary representation of the object
   * @param <T> the type of the object
   * @throws Exception thrown if the object cannot be serialized
   */
  <T> byte[] Serialize(T object) throws Exception;

  /**
   * Deserialize a byte array to an object
   * 
   * @param redisObj the byte array to deserialize
   * @return the object
   * @param <T> the type of the object
   * @throws Exception thrown if the object cannot be deserialized
   */
  <T> T Deserialize(byte[] redisObj) throws Exception;
}
