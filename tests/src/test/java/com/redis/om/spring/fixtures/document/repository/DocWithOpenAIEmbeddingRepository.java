package com.redis.om.spring.fixtures.document.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.document.model.DocWithOpenAIEmbedding;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DocWithOpenAIEmbeddingRepository extends RedisDocumentRepository<DocWithOpenAIEmbedding, String> {
  Optional<DocWithOpenAIEmbedding> findFirstByName(String name);
}
