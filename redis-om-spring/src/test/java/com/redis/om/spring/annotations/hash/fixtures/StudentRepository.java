package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.repository.RedisEnhancedRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StudentRepository extends RedisEnhancedRepository<Student, Long> {
  List<Student> findByUserName(String userName);
  List<Student> findByUserNameAndEventTimestampBetweenOrderByEventTimestampAsc(String userName, LocalDateTime fromDate, LocalDateTime toDate);
}
