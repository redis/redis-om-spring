package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocWithCustomModelOpenAIEmbedding;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Optional;

public interface DocWithCustomModelOpenAIEmbeddingRepository extends RedisDocumentRepository<DocWithCustomModelOpenAIEmbedding, String> {
  Optional<DocWithCustomModelOpenAIEmbedding> findFirstByName(String name);
}
