package com.redis.om.spring.repository;

import java.util.Collection;
import java.util.Optional;

public interface DocumentProjectionRepository extends RedisDocumentRepository<DocumentProjectionPojo, String> {
   Optional<DocumentProjection> findByName(String name);
   Collection<DocumentProjection> findAllByName(String name);
}
