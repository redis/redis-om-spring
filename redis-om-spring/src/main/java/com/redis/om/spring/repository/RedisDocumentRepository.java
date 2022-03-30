package com.redis.om.spring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface RedisDocumentRepository<T, ID> extends PagingAndSortingRepository<T, ID> {
  /**
   * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
   *
   * @param pageable
   * @return a page of entities
   */
  Iterable<ID> getIds();
  
  Page<ID> getIds(Pageable pageable);
}
