package com.redis.om.spring.fixtures.document.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.document.model.NiCompany;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface NiCompanyRepository extends RedisDocumentRepository<NiCompany, String> {
  Optional<NiCompany> findFirstByName(String name);
}
