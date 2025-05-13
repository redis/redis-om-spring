package com.redis.om.documents.repositories;

import java.util.List;

import com.redis.om.documents.domain.Person;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface PersonRepository extends RedisDocumentRepository<Person, String> {
  List<Person> findByLastNameAndFirstName(String lastName, String firstName);
}
