package com.redis.om.spring.annotations.hash.fixtures;

import java.util.Optional;

public interface MyHashQueries {
  Optional<MyHash> findByTitle(String title);
}
