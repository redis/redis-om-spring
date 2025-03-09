package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Custom;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;

@SuppressWarnings("unused")
public interface CustomRepository extends RedisDocumentRepository<Custom, Long> {
  List<Custom> searchByName(String name);
}
