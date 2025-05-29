package com.redis.om.spring.search.stream;

/**
 * Interface for creating entity search streams.
 */
public interface EntityStream {
  /**
   * Create search stream for entity class.
   * 
   * @param <E>         entity type
   * @param entityClass the entity class
   * @return search stream for the entity
   */
  <E> SearchStream<E> of(final Class<E> entityClass);

  /**
   * Create search stream with custom index and ID field.
   * 
   * @param <E>         entity type
   * @param entityClass the entity class
   * @param searchIndex the search index name
   * @param idField     the ID field name
   * @return search stream for the entity
   */
  <E> SearchStream<E> of(final Class<E> entityClass, String searchIndex, String idField);
}
