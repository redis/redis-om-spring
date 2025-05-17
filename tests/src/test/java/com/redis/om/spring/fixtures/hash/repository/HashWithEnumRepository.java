package com.redis.om.spring.fixtures.hash.repository;

import java.util.List;

import com.redis.om.spring.fixtures.document.model.MyJavaEnum;
import com.redis.om.spring.fixtures.hash.model.HashWithEnum;
import com.redis.om.spring.repository.RedisEnhancedRepository;

@SuppressWarnings(
  "unused"
)
public interface HashWithEnumRepository extends RedisEnhancedRepository<HashWithEnum, String> {
  List<HashWithEnum> findByEnumProp(MyJavaEnum value);
}
