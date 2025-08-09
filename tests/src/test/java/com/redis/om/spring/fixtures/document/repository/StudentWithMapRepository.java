package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.StudentWithMap;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;

public interface StudentWithMapRepository extends RedisDocumentRepository<StudentWithMap, String> {
  // Test query methods for Map fields - queries work on the indexed map values
  List<StudentWithMap> findByCourseGradesGreaterThan(int grade);
  List<StudentWithMap> findByCourseInstructorsContaining(String instructor);
  // Alternative approach - try exact matching instead of substring
  List<StudentWithMap> findByCourseInstructors(String instructor);
}