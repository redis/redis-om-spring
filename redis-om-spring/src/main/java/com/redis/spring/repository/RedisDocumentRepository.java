package com.redis.spring.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.redislabs.modules.rejson.Path;

@NoRepositoryBean
public interface RedisDocumentRepository<T, ID> extends PagingAndSortingRepository<T, ID> {
  
  /**
   * Retrieves an entity by its id.
   *
   * @param id must not be {@literal null}.
   * @param paths optional one ore more paths in the object
   * @return the entity with the given id or {@literal Optional#empty()} if none found.
   * @throws IllegalArgumentException if {@literal id} is {@literal null}.
   */
  Optional<T> getById(ID id, Path... paths);
  
  /**
   * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
   *
   * @param pageable
   * @return a page of entities
   */
  Page<ID> getIds(Pageable pageable);
}
