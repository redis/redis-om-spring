package com.redis.spring.annotations.document.fixtures;

import java.util.List;

import com.redis.spring.repository.RedisDocumentRepository;

public interface CompanyRepository extends RedisDocumentRepository<Company, String> {
  List<Company> findByName(String companyName);
}
