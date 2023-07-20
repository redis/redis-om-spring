package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.repository.RedisEnhancedRepository;
import org.springframework.stereotype.Repository;

@SuppressWarnings({ "unused", "SpellCheckingInspection", "SpringDataMethodInconsistencyInspection" }) @Repository
public interface Person2Repository extends RedisEnhancedRepository<Person2, String> {
  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);
}
