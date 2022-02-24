package com.redis.om.spring.search.stream.predicates;

public enum PredicateType {
  // Comparable
  EQUAL,
  NOT_EQUAL,
  IN,

  // Boolean chaining
  OR,
  AND,

  // Numeric
  GREATER_THAN,
  LESS_THAN,
  LESS_THAN_OR_EQUAL,
  GREATER_THAN_OR_EQUAL,
  BETWEEN,

  // Geo
  NEAR,

  // Text
  STARTS_WITH,
  ENDS_WITH,
  LIKE,
  NOT_LIKE,
  CONTAINS_ALL,
}
