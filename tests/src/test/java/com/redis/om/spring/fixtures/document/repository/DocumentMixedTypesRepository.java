package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocumentWithMixedTypes;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Optional;

public interface DocumentMixedTypesRepository extends RedisDocumentRepository<DocumentWithMixedTypes, String> {
  
  // Return the entity first to verify data exists
  Optional<DocumentWithMixedTypes> findByName(String name);
  
  // Return projection without @Value annotations
  Optional<DocumentMixedTypesProjection> findFirstByName(String name);
  
  // Return projection with @Value annotations  
  Optional<DocumentMixedTypesProjectionFixed> findOneByName(String name);
}