package com.redis.om.spring.annotations.hash.fixtures;

import org.springframework.data.repository.CrudRepository;

public interface ExpiringPersonRepository extends CrudRepository<ExpiringPerson, String> {
}
