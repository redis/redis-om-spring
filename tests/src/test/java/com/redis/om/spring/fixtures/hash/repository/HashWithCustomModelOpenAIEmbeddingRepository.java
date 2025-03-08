package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.document.model.DocWithCustomModelOpenAIEmbedding;
import com.redis.om.spring.fixtures.hash.model.HashWithCustomModelOpenAIEmbedding;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Optional;

public interface HashWithCustomModelOpenAIEmbeddingRepository extends RedisDocumentRepository<HashWithCustomModelOpenAIEmbedding, String> {
  Optional<HashWithCustomModelOpenAIEmbedding> findFirstByName(String name);
}
