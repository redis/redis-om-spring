package com.redis.om.spring.annotations.bloom.fixtures;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import io.redisearch.Suggestion;

@Repository
public interface PersonRepository extends CrudRepository<Person, String>, EmailTaken {
  boolean existsByEmail(String email);

  List<Suggestion> autoCompleteEmail(String string);
}

