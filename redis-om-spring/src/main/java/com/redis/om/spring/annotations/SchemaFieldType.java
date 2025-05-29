package com.redis.om.spring.annotations;

/**
 * Enumeration defining the types of schema fields supported by RediSearch.
 * These types determine how fields are indexed and can be searched in Redis.
 * 
 * @since 1.0.0
 */
public enum SchemaFieldType {
  /**
   * Automatically detect the field type based on the Java field type and annotations.
   */
  AUTODETECT,

  /**
   * Tag field type for exact match searches and faceted queries.
   * Tags are useful for categorical data and filtering.
   */
  TAG,

  /**
   * Numeric field type for numeric range queries and sorting.
   * Supports integer and floating-point numbers.
   */
  NUMERIC,

  /**
   * Geographic field type for geospatial queries and operations.
   * Stores longitude and latitude coordinates.
   */
  GEO,

  /**
   * Vector field type for vector similarity search operations.
   * Used for AI/ML applications and semantic search.
   */
  VECTOR,

  /**
   * Nested field type for complex object structures.
   * Allows indexing of nested JSON objects.
   */
  NESTED
}
