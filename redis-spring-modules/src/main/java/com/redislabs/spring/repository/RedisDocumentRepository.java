package com.redislabs.spring.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface RedisDocumentRepository<T, ID> extends PagingAndSortingRepository<T, ID> {
  @Override
  long count();

  @Override
  void delete(T entity);

  @Override
  void deleteAll();

  @Override
  void deleteAll(Iterable<? extends T> entities);

  @Override
  void deleteAllById(Iterable<? extends ID> ids);

  @Override
  void deleteById(ID id);

  @Override
  boolean existsById(ID id);

  @Override
  Iterable<T> findAll();

  @Override
  Page<T> findAll(Pageable pageable);

  @Override
  Iterable<T> findAll(Sort sort);

  @Override
  Iterable<T> findAllById(Iterable<ID> ids);

  @Override
  Optional<T> findById(ID id);

  @Override
  <S extends T> S save(S entity);

  @Override
  <S extends T> Iterable<S> saveAll(Iterable<S> entities);

}
