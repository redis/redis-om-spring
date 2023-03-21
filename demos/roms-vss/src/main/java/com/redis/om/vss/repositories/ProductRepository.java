package com.redis.om.vss.repositories;

import com.redis.om.spring.repository.RedisEnhancedRepository;
import com.redis.om.vss.domain.Product;

public interface ProductRepository extends RedisEnhancedRepository<Product, String> {
}
