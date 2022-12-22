package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Set;

@SuppressWarnings({ "unused", "SpringDataRepositoryMethodParametersInspection" }) public interface MetadataRepository extends RedisDocumentRepository<Metadata, String> {
  Iterable<Metadata> findByDeptId(Set<String> list);
}
