package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.Product;
import com.redis.om.spring.repository.RedisEnhancedRepository;

import java.util.Optional;

public interface ProductRepository extends RedisEnhancedRepository<Product, String> {
  Optional<Product> findFirstByName(String name);
}
