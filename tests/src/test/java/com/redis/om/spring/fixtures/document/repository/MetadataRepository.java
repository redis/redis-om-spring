package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Metadata;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Set;

@SuppressWarnings({ "unused", "SpringDataRepositoryMethodParametersInspection" })
public interface MetadataRepository extends RedisDocumentRepository<Metadata, String> {
  Iterable<Metadata> findByDeptId(Set<String> list);
}
