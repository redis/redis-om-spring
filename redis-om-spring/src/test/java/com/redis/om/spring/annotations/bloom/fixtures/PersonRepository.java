package com.redis.om.spring.annotations.bloom.fixtures;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends CrudRepository<Person, String>, EmailTaken {
  boolean existsByEmail(String email);
}

