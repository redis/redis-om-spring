package com.redislabs.spring.repository.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.redislabs.spring.repository.RedisDocumentRepository;

public class RedisDocumentRepositoryImpl<T, ID> implements RedisDocumentRepository<T, ID> {

  @Override
  public long count() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void delete(T entity) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteAll() {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteAll(Iterable<? extends T> entities) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteAllById(Iterable<? extends ID> ids) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteById(ID id) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean existsById(ID id) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Iterable<T> findAll() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Page<T> findAll(Pageable pageable) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Iterable<T> findAll(Sort sort) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Iterable<T> findAllById(Iterable<ID> ids) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<T> findById(ID id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <S extends T> S save(S entity) {
    System.out.println(">>>> Saving... " + entity);
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
    // TODO Auto-generated method stub
    return null;
  }
}
