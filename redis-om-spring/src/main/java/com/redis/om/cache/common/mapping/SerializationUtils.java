package com.redis.om.cache.common.mapping;

import org.springframework.lang.Nullable;

/**
 * Utility class providing helper methods for serialization operations.
 */
public abstract class SerializationUtils {

  /**
   * Constant representing an empty byte array.
   */
  public static final byte[] EMPTY_ARRAY = new byte[0];

  /**
   * Checks if the given byte array is null or empty.
   *
   * @param data the byte array to check
   * @return true if the array is null or empty, false otherwise
   */
  public static boolean isEmpty(@Nullable byte[] data) {
    return (data == null || data.length == 0);
  }
}
