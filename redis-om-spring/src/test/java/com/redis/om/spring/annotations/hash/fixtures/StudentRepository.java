package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.repository.RedisEnhancedRepository;

import java.util.List;

public interface StudentRepository extends RedisEnhancedRepository<Student, Long> {
  List<Student> findByUserName(String userName);
}
