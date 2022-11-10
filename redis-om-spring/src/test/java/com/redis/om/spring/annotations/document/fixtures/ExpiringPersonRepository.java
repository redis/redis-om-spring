package com.redis.om.spring.annotations.document.fixtures;

import java.util.Optional;

import com.redis.om.spring.repository.RedisDocumentRepository;

public interface ExpiringPersonRepository extends RedisDocumentRepository<ExpiringPerson, String> {
  Optional<ExpiringPerson> findOneByName(String name);
}
