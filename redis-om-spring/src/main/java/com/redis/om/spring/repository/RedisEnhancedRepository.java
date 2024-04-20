package com.redis.om.spring.repository;

import com.redis.om.spring.metamodel.MetamodelField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.QueryByExampleExecutor;

@NoRepositoryBean
public interface RedisEnhancedRepository<T, ID> extends KeyValueRepository<T, ID>, QueryByExampleExecutor<T> {

  Iterable<ID> getIds();

  /**
   * Returns a {@link Page} of ids meeting the paging restriction provided in
   * the {@code Pageable} object.
   *
   * @param pageable encapsulates pagination information
   * @return a page of ids
   */
  Page<ID> getIds(Pageable pageable);

  void updateField(T entity, MetamodelField<T, ?> field, Object value);

  <F> Iterable<F> getFieldsByIds(Iterable<ID> ids, MetamodelField<T, F> field);

  Long getExpiration(ID id);

  String getKeyspace();
}
