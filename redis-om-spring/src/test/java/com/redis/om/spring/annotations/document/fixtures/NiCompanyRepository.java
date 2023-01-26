package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Optional;

@SuppressWarnings("unused") public interface NiCompanyRepository extends RedisDocumentRepository<NiCompany, String> {
  Optional<NiCompany> findFirstByName(String name);
}
