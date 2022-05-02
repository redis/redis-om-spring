package com.redis.om.spring.annotations.document.fixtures;

import java.util.Set;

import com.redis.om.spring.repository.RedisDocumentRepository;

public interface MetadataRepository extends RedisDocumentRepository<Metadata, String> {
  Iterable<Metadata> findByDeptId(Set<String> list);
}
