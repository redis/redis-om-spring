package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocumentWithMixedTypes;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Collection;
import java.util.Optional;

public interface DocumentMixedTypesRepository extends RedisDocumentRepository<DocumentWithMixedTypes, String> {
  
  // Projection without @Value annotations - following working test pattern
  Optional<DocumentMixedTypesProjection> findByName(String name);
  
  // Projection with @Value annotations 
  Collection<DocumentMixedTypesProjectionFixed> findAllByName(String name);
}