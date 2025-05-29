package com.redis.om.spring.repository.query;

/**
 * Enumeration of the different types of RediSearch queries supported by Redis OM Spring.
 * <p>
 * This enum defines the various query execution strategies available when interacting
 * with Redis through the RediSearch module. Each type corresponds to a different
 * RediSearch command with specific capabilities and use cases.
 * </p>
 *
 * @see RediSearchQuery
 * @see RediSearchQueryCreator
 * @since 0.1.0
 */
public enum RediSearchQueryType {
  /**
   * Standard search query using FT.SEARCH command.
   * Used for basic text search, filtering, and sorting operations.
   */
  QUERY,
  /**
   * Aggregation query using FT.AGGREGATE command.
   * Used for complex data analysis operations like grouping, reducing, and transforming results.
   */
  AGGREGATION,
  /**
   * Tag values query using FT.TAGVALS command.
   * Used to retrieve all possible values for a specific tag field in the index.
   */
  TAGVALS,
  /**
   * Autocomplete query using FT.SUGGET command.
   * Used for autocomplete functionality and suggestion retrieval.
   */
  AUTOCOMPLETE,
  /**
   * Delete query for removing documents from the search index.
   * Used to delete documents that match specific search criteria.
   */
  DELETE,
}
