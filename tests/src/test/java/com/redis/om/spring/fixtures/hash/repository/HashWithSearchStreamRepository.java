package com.redis.om.spring.fixtures.hash.repository;

import java.util.Set;

import org.springframework.stereotype.Repository;

import com.redis.om.spring.fixtures.hash.model.HashWithSearchStream;
import com.redis.om.spring.repository.RedisEnhancedRepository;
import com.redis.om.spring.search.stream.SearchStream;

@Repository
public interface HashWithSearchStreamRepository extends RedisEnhancedRepository<HashWithSearchStream, String> {
  
  // Methods that return SearchStream for testing
  SearchStream<HashWithSearchStream> findByEmail(String email);
  
  SearchStream<HashWithSearchStream> findByDepartment(String department);
  
  SearchStream<HashWithSearchStream> findByAgeGreaterThan(Integer age);
  
  SearchStream<HashWithSearchStream> findByActive(Boolean active);
  
  SearchStream<HashWithSearchStream> findBySkills(Set<String> skills);
}