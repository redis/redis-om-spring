package com.redis.om.spring.annotations;

/**
 * Enumeration of serialization hints for Redis OM Spring entity fields.
 * <p>
 * This enum provides hints about how specific field types should be serialized
 * when storing data in Redis. Different serialization strategies can optimize
 * storage space, query performance, or compatibility with Redis Search.
 * </p>
 */
public enum SerializationHint {

  /**
   * Serialize using ordinal values.
   * <p>
   * For enum types, this hint indicates that the enum should be serialized
   * as its ordinal integer value rather than its string representation.
   * This can provide more compact storage and faster comparisons.
   * </p>
   */
  ORDINAL,

  /**
   * No specific serialization hint.
   * <p>
   * The default serialization behavior should be used, typically meaning
   * string representation for enums and standard JSON serialization for
   * other types.
   * </p>
   */
  NONE
}
