package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.City;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface CityRepository extends RedisDocumentRepository<City, String> {
}
