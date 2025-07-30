package com.redis.om.spring.search.stream.predicates.lexicographic;

/**
 * Marker interface for predicates that require lexicographic index support.
 * These predicates need special handling in the SearchStream to access
 * the lexicographic sorted sets.
 * 
 * @since 1.0
 */
public interface LexicographicPredicate {
}