package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Language;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Optional;

public interface LanguageRepository extends RedisDocumentRepository<Language, Integer> {
  Optional<Language> findOneByName(String name);
}
