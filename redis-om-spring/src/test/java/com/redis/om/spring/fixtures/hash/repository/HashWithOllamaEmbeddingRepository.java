package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.HashWithOllamaEmbedding;
import com.redis.om.spring.repository.RedisEnhancedRepository;

import java.util.Optional;

public interface HashWithOllamaEmbeddingRepository extends RedisEnhancedRepository<HashWithOllamaEmbedding, String> {
  Optional<HashWithOllamaEmbedding> findFirstByName(String name);
}
