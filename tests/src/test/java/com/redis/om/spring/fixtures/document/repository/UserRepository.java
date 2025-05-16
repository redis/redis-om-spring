package com.redis.om.spring.fixtures.document.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.document.model.User;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface UserRepository extends RedisDocumentRepository<User, String> {
  Optional<User> findFirstByName(String name);
}
