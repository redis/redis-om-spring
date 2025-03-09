package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocWithOllamaEmbedding;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Optional;

public interface DocWithOllamaEmbeddingRepository extends RedisDocumentRepository<DocWithOllamaEmbedding, String> {
  Optional<DocWithOllamaEmbedding> findFirstByName(String name);
}
