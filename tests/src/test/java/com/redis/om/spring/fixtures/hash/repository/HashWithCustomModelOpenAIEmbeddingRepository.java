package com.redis.om.spring.fixtures.hash.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.hash.model.HashWithCustomModelOpenAIEmbedding;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface HashWithCustomModelOpenAIEmbeddingRepository extends
    RedisDocumentRepository<HashWithCustomModelOpenAIEmbedding, String> {
  Optional<HashWithCustomModelOpenAIEmbedding> findFirstByName(String name);
}
