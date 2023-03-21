package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.repository.RedisEnhancedRepository;

import java.util.Optional;

public interface ProductRepository extends RedisEnhancedRepository<Product, String> {
  Optional<Product> findFirstByName(String name);
}
