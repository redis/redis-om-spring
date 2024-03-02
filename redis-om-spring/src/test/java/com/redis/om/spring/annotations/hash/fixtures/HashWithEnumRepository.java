package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.annotations.document.fixtures.MyJavaEnum;
import com.redis.om.spring.repository.RedisEnhancedRepository;

import java.util.List;

@SuppressWarnings("unused")
public interface HashWithEnumRepository extends RedisEnhancedRepository<HashWithEnum, String> {
  List<HashWithEnum> findByEnumProp(MyJavaEnum value);
}
