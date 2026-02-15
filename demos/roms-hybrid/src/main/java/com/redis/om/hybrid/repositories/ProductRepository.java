package com.redis.om.hybrid.repositories;

import com.redis.om.hybrid.domain.Product;
import com.redis.om.spring.repository.RedisEnhancedRepository;

/**
 * Repository for Product entities.
 *
 * Extends RedisEnhancedRepository to provide enhanced search capabilities
 * including hybrid search combining text and vector similarity.
 */
public interface ProductRepository extends RedisEnhancedRepository<Product, String> {
}
