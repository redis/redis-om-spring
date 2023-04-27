package com.redis.om.documents.repositories;

import com.redis.om.documents.domain.Person;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;

public interface PersonRepository extends RedisDocumentRepository<Person, String> {
  List<Person> findByLastNameAndFirstName(String lastName, String firstName);
}
