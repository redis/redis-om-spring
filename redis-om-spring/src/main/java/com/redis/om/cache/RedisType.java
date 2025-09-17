package com.redis.om.cache;

/**
 * Enum representing the different Redis data structure types that can be used for caching.
 */
public enum RedisType {

  /**
   * Redis Hash data structure, storing field-value pairs.
   */
  HASH,

  /**
   * Redis String data structure, storing simple string values.
   */
  STRING,

  /**
   * Redis JSON data structure, storing JSON documents.
   */
  JSON

}
