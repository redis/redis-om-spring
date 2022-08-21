package com.redis.om.spring.annotations.document.fixtures;

import java.util.Optional;

import com.redis.om.spring.repository.RedisDocumentRepository;

public interface NiCompanyRepository extends RedisDocumentRepository<NiCompany, String> {
  Optional<NiCompany> findFirstByName(String name);
}
