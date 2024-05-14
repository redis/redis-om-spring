package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;

public interface StudentRepository extends RedisDocumentRepository<Student, Long> {
  List<Student> findByUserName(String userName);
}
