package com.redis.om.spring.annotations.document.fixtures;

import java.util.List;

import com.redis.om.spring.repository.RedisDocumentRepository;

public interface CompanyRepository extends RedisDocumentRepository<Company, String> {
  List<Company> findByName(String companyName);
  boolean existsByEmail(String email);

  List<Company> findByEmployees_name(String name);
}
