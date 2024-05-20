package com.redis.om.spring.fixtures.hash.repository;


import com.redis.om.spring.fixtures.document.model.MyJavaEnum;
import com.redis.om.spring.fixtures.hash.model.HashWithEnum;
import com.redis.om.spring.repository.RedisEnhancedRepository;

import java.util.List;

@SuppressWarnings("unused")
public interface HashWithEnumRepository extends RedisEnhancedRepository<HashWithEnum, String> {
  List<HashWithEnum> findByEnumProp(MyJavaEnum value);
}
