package com.redis.om.spring.fixtures.document.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.document.model.DocWithOllamaEmbedding;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DocWithOllamaEmbeddingRepository extends RedisDocumentRepository<DocWithOllamaEmbedding, String> {
  Optional<DocWithOllamaEmbedding> findFirstByName(String name);
}
