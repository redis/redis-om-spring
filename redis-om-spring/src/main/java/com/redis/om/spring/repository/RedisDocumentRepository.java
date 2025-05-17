package com.redis.om.spring.repository;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import com.redis.om.spring.metamodel.MetamodelField;

import redis.clients.jedis.json.Path2;

@NoRepositoryBean
public interface RedisDocumentRepository<T, ID> extends KeyValueRepository<T, ID>, QueryByExampleExecutor<T> {

  Iterable<ID> getIds();

  /**
   * Returns a {@link Page} of ids meeting the paging restriction provided in
   * the {@code Pageable} object.
   *
   * @param pageable encapsulates pagination information
   * @return a page of ids
   */
  Page<ID> getIds(Pageable pageable);

  void deleteById(ID id, Path2 path);

  void updateField(T entity, MetamodelField<T, ?> field, Object value);

  <F> Iterable<F> getFieldsByIds(Iterable<ID> ids, MetamodelField<T, F> field);

  Long getExpiration(ID id);

  boolean setExpiration(ID id, Long expiration, TimeUnit timeUnit);

  Iterable<T> bulkLoad(String file) throws IOException;

  <S extends T> S update(S entity);

  String getKeyspace();

  // QBE Extensions

  <S extends T> S update(Example<S> example);

  <S extends T> void updateAll(Iterable<Example<S>> examples);

  // Key utilities

  String getKeyFor(T entity);
}
