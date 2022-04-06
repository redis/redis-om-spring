package com.redis.om.spring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.redis.om.spring.metamodel.FieldOperationInterceptor;
import com.redislabs.modules.rejson.Path;

@NoRepositoryBean
public interface RedisDocumentRepository<T, ID> extends KeyValueRepository<T, ID> {
  /**
   * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
   *
   * @param pageable
   * @return a page of entities
   */
  Iterable<ID> getIds();
  
  Page<ID> getIds(Pageable pageable);
  
  void deleteById(ID id, Path path);
  
  void updateField(T entity, FieldOperationInterceptor<T, ?> field, Object value);
}
