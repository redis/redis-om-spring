package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Person;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface PersonRepository extends RedisDocumentRepository<Person, String> {
}
