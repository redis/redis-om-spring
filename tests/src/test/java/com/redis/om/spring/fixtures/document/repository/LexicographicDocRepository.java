package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.LexicographicDoc;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;

public interface LexicographicDocRepository extends RedisDocumentRepository<LexicographicDoc, String> {
  
  // SKU field queries (TAG field with lexicographic=true)
  List<LexicographicDoc> findBySkuGreaterThan(String sku);
  List<LexicographicDoc> findBySkuLessThan(String sku);
  List<LexicographicDoc> findBySkuGreaterThanEqual(String sku);
  List<LexicographicDoc> findBySkuLessThanEqual(String sku);
  List<LexicographicDoc> findBySkuBetween(String min, String max);
  
  // Name field queries (TEXT field with lexicographic=true)
  List<LexicographicDoc> findByNameGreaterThan(String name);
  List<LexicographicDoc> findByNameLessThan(String name);
  List<LexicographicDoc> findByNameBetween(String min, String max);
  
  // Category field queries (TAG field with lexicographic=true)
  List<LexicographicDoc> findByCategoryGreaterThan(String category);
  List<LexicographicDoc> findByCategoryLessThan(String category);
  List<LexicographicDoc> findByCategoryBetween(String min, String max);
  
  // Status field queries (TAG field with lexicographic=false)
  // These should work as normal TAG queries
  List<LexicographicDoc> findByStatus(String status);
}