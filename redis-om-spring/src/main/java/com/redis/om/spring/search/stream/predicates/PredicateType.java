package com.redis.om.spring.search.stream.predicates;

/**
 * Enumeration of predicate types used in Redis OM Spring search operations.
 * <p>
 * This enum defines the various types of predicates that can be used when building
 * search queries through the Entity Streams API. Each predicate type corresponds to
 * a specific type of comparison or logical operation that can be performed on indexed
 * fields in Redis search queries.
 * </p>
 * <p>
 * The predicate types are organized into categories:
 * <ul>
 * <li><strong>Comparable:</strong> Basic equality and membership operations</li>
 * <li><strong>Boolean chaining:</strong> Logical operators for combining predicates</li>
 * <li><strong>Numeric:</strong> Range and comparison operations for numeric fields</li>
 * <li><strong>Geo:</strong> Geospatial proximity operations</li>
 * <li><strong>Text:</strong> String matching and text search operations</li>
 * </ul>
 *
 * @see com.redis.om.spring.search.stream.EntityStream
 * @since 0.1.0
 */
public enum PredicateType {
  // Comparable

  /** Exact equality comparison for any comparable field type */
  EQUAL,

  /** Inequality comparison for any comparable field type */
  NOT_EQUAL,

  /** Membership test - checks if a field value is within a specified set of values */
  IN,

  // Boolean chaining

  /** Logical OR operation for combining multiple predicates */
  OR,

  /** Logical AND operation for combining multiple predicates */
  AND,

  // Numeric

  /** Greater than comparison for numeric fields */
  GREATER_THAN,

  /** Less than comparison for numeric fields */
  LESS_THAN,

  /** Less than or equal comparison for numeric fields */
  LESS_THAN_OR_EQUAL,

  /** Greater than or equal comparison for numeric fields */
  GREATER_THAN_OR_EQUAL,

  /** Range comparison - checks if a numeric value falls within a specified range */
  BETWEEN,

  // Geo

  /** Geospatial proximity search - finds points within a specified distance */
  NEAR,

  // Text

  /** String prefix matching - checks if text starts with specified value */
  STARTS_WITH,

  /** String suffix matching - checks if text ends with specified value */
  ENDS_WITH,

  /** Pattern matching with wildcard support for text fields */
  LIKE,

  /** Negated pattern matching - excludes matches for specified pattern */
  NOT_LIKE,

  /** Full-text search - checks if text contains all specified terms */
  CONTAINS_ALL
}
