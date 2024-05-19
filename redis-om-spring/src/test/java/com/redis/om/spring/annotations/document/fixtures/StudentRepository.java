package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StudentRepository extends RedisDocumentRepository<Student, Long> {
  List<Student> findByUserName(String userName);
  List<Student> findByUserNameAndEventTimestampBetweenOrderByEventTimestampAsc(String userName, LocalDateTime fromDate, LocalDateTime toDate);
}
