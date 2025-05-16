package com.redis.om.spring.fixtures.hash.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.hash.model.Product;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface ProductRepository extends RedisEnhancedRepository<Product, String> {
  Optional<Product> findFirstByName(String name);
}
