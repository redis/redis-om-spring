package com.redis.om.spring.fixtures.document.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.document.model.Language;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface LanguageRepository extends RedisDocumentRepository<Language, Integer> {
  Optional<Language> findOneByName(String name);
}
