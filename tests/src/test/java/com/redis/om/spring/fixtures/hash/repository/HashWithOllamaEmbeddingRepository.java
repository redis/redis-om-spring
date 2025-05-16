package com.redis.om.spring.fixtures.hash.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.hash.model.HashWithOllamaEmbedding;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface HashWithOllamaEmbeddingRepository extends RedisEnhancedRepository<HashWithOllamaEmbedding, String> {
  Optional<HashWithOllamaEmbedding> findFirstByName(String name);
}
