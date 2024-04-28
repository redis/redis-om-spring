package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Optional;

public interface LanguageRepository extends RedisDocumentRepository<Language, Integer> {
  Optional<Language> findOneByName(String name);
}
