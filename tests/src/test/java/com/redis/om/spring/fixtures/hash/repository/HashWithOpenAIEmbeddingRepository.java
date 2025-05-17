package com.redis.om.spring.fixtures.hash.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.hash.model.HashWithOpenAIEmbedding;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface HashWithOpenAIEmbeddingRepository extends RedisEnhancedRepository<HashWithOpenAIEmbedding, String> {
  Optional<HashWithOpenAIEmbedding> findFirstByName(String name);
}
