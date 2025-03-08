package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.MyHash;

import java.util.Optional;

public interface MyHashQueries {
  Optional<MyHash> findByTitle(String title);
}
