package com.redis.om.spring.fixtures.document.repository;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.redis.om.spring.annotations.Query;
import com.redis.om.spring.fixtures.document.model.ProductDoc;
import com.redis.om.spring.repository.RedisDocumentRepository;

import redis.clients.jedis.search.SearchResult;

/**
 * Repository for testing Issue #676: Gson JSON serialization bug with spaces in projected fields.
 */
public interface ProductDocRepository extends RedisDocumentRepository<ProductDoc, String> {

  /**
   * Query that returns projected fields (keyword, category) as domain objects.
   * This triggers the parseDocumentResult projection logic path.
   */
  @Query(value = "@category:{$category}", returnFields = {"keyword", "category"})
  List<ProductDoc> findByCategoryWithProjection(@Param("category") String category);

  /**
   * Baseline query returning SearchResult to verify search works.
   */
  @Query(value = "@category:{$category}", returnFields = {"keyword", "category"})
  SearchResult findByCategoryReturningSearchResult(@Param("category") String category);
}
