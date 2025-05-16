package com.redis.om.spring.fixtures.hash.repository;

import org.springframework.stereotype.Repository;

import com.redis.om.spring.fixtures.hash.model.Person2;
import com.redis.om.spring.repository.RedisEnhancedRepository;

@SuppressWarnings(
  { "unused", "SpellCheckingInspection", "SpringDataMethodInconsistencyInspection" }
)
@Repository
public interface Person2Repository extends RedisEnhancedRepository<Person2, String> {
  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);
}
