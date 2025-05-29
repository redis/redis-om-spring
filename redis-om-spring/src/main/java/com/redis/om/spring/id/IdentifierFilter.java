package com.redis.om.spring.id;

/**
 * Interface for filtering identifiers before storing them in Redis.
 *
 * @param <ID> the type of identifier to filter
 */
public interface IdentifierFilter<ID> {
  /**
   * Filters the given identifier.
   *
   * @param id the identifier to filter
   * @return the filtered identifier as a string
   */
  String filter(ID id);
}
