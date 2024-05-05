package com.redis.om.spring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Optional;

public interface DocumentProjectionRepository extends RedisDocumentRepository<DocumentProjectionPojo, String> {
   Optional<DocumentProjection> findByName(String name);
   Collection<DocumentProjection> findAllByName(String name);
   Page<DocumentProjection> findAllByName(String name, Pageable pageable);
}
