package com.redis.om.multitenant.repositories;

import java.util.List;

import com.redis.om.multitenant.domain.Product;
import com.redis.om.spring.repository.RedisDocumentRepository;

/**
 * Repository for Product entities.
 *
 * <p>All operations are automatically scoped to the current tenant's index
 * based on the SpEL expression in the @IndexingOptions annotation.
 */
public interface ProductRepository extends RedisDocumentRepository<Product, String> {

  /**
   * Find products by category.
   */
  List<Product> findByCategory(String category);

  /**
   * Find active products.
   */
  List<Product> findByActiveTrue();

  /**
   * Find products by price range.
   */
  List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

  /**
   * Full-text search by name.
   */
  List<Product> findByNameContaining(String name);

  /**
   * Find products by category and active status.
   */
  List<Product> findByCategoryAndActiveTrue(String category);

  /**
   * Find products with stock below threshold.
   */
  List<Product> findByStockQuantityLessThan(Integer threshold);

  /**
   * Find products by SKU prefix.
   */
  List<Product> findBySkuStartingWith(String prefix);
}
