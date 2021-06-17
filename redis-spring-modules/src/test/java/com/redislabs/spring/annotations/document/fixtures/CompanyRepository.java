package com.redislabs.spring.annotations.document.fixtures;

import com.redislabs.spring.repository.RedisDocumentRepository;

public interface CompanyRepository extends RedisDocumentRepository<Company, Integer> {

}
