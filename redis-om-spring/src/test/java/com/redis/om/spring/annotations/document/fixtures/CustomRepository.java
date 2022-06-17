package com.redis.om.spring.annotations.document.fixtures;

import java.util.List;

import com.redis.om.spring.repository.RedisDocumentRepository;

public interface CustomRepository extends RedisDocumentRepository<Custom, Long> {
  List<Custom> searchByName(String name);
}
