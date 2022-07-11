package com.redis.om.spring.annotations.document.fixtures;

import java.util.Optional;

import com.redis.om.spring.repository.RedisDocumentRepository;

public interface UserRepository  extends RedisDocumentRepository<User, String> {
  Optional<User> findFirstByName(String name);
}
