package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocumentProjectionPojo;
import com.redis.om.spring.repository.RedisDocumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Optional;

public interface DocumentProjectionRepository extends RedisDocumentRepository<DocumentProjectionPojo, String> {
  Optional<DocumentProjection> findByName(String name);

  Collection<DocumentProjection> findAllByName(String name);

  Page<DocumentProjection> findAllByName(String name, Pageable pageable);
}
