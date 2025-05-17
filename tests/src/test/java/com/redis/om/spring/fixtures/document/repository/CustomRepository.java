package com.redis.om.spring.fixtures.document.repository;

import java.util.List;

import com.redis.om.spring.fixtures.document.model.Custom;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface CustomRepository extends RedisDocumentRepository<Custom, Long> {
  List<Custom> searchByName(String name);
}
