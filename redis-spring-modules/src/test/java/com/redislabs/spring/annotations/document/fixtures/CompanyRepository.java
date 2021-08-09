package com.redislabs.spring.annotations.document.fixtures;

import java.util.List;

import com.redislabs.spring.repository.RedisDocumentRepository;

public interface CompanyRepository extends RedisDocumentRepository<Company, String> {
  List<Company> findByName(String companyName);
}
