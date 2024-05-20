package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Country;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface CountryRepository extends RedisDocumentRepository<Country, String> {
}
