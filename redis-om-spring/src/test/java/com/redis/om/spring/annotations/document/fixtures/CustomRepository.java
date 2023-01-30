package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;

@SuppressWarnings("unused") public interface CustomRepository extends RedisDocumentRepository<Custom, Long> {
  List<Custom> searchByName(String name);
}
