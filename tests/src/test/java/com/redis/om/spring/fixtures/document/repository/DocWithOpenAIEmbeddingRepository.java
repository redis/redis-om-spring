package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocWithOpenAIEmbedding;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Optional;

public interface DocWithOpenAIEmbeddingRepository extends RedisDocumentRepository<DocWithOpenAIEmbedding, String> {
  Optional<DocWithOpenAIEmbedding> findFirstByName(String name);
}
