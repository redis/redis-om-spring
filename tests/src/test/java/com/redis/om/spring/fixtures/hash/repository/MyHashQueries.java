package com.redis.om.spring.fixtures.hash.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.hash.model.MyHash;

public interface MyHashQueries {
  Optional<MyHash> findByTitle(String title);
}
