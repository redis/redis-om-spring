package com.redis.om.spring.fixtures.document.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.document.model.ExpiringPerson;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface ExpiringPersonRepository extends RedisDocumentRepository<ExpiringPerson, String> {
  Optional<ExpiringPerson> findOneByName(String name);
}
