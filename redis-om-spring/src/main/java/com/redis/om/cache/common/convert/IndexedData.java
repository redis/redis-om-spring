package com.redis.om.cache.common.convert;

/**
 * {@link IndexedData} represents a secondary index for a property path in a given keyspace.
 *
 */
public interface IndexedData {

  /**
   * Get the {@link String} representation of the index name.
   *
   * @return never {@literal null}.
   */
  String getIndexName();

  /**
   * Get the associated keyspace the index resides in.
   *
   * @return the keyspace name, never {@literal null}.
   */
  String getKeyspace();

}
