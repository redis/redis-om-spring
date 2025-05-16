package com.redis.om.spring.fixtures.document.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.document.model.DocWithCustomModelOpenAIEmbedding;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DocWithCustomModelOpenAIEmbeddingRepository extends
    RedisDocumentRepository<DocWithCustomModelOpenAIEmbedding, String> {
  Optional<DocWithCustomModelOpenAIEmbedding> findFirstByName(String name);
}
