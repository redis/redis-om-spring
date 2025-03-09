package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.document.model.DocWithOpenAIEmbedding;
import com.redis.om.spring.fixtures.hash.model.HashWithOpenAIEmbedding;
import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redis.om.spring.repository.RedisEnhancedRepository;

import java.util.Optional;

public interface HashWithOpenAIEmbeddingRepository extends RedisEnhancedRepository<HashWithOpenAIEmbedding, String> {
  Optional<HashWithOpenAIEmbedding> findFirstByName(String name);
}
