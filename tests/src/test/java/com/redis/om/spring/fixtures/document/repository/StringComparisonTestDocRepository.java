package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.StringComparisonTestDoc;
import com.redis.om.spring.repository.RedisDocumentRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StringComparisonTestDocRepository extends RedisDocumentRepository<StringComparisonTestDoc, String> {
  
  List<StringComparisonTestDoc> findByIndexedStringFieldGreaterThan(String value);
  
  List<StringComparisonTestDoc> findByIndexedStringFieldLessThan(String value);
  
  List<StringComparisonTestDoc> findByIndexedStringFieldGreaterThanEqual(String value);
  
  List<StringComparisonTestDoc> findByIndexedStringFieldLessThanEqual(String value);
  
  List<StringComparisonTestDoc> findByComIdGreaterThan(String comId);
  
  List<StringComparisonTestDoc> findByComIdLessThan(String comId);
  
  List<StringComparisonTestDoc> findByComIdBetween(String start, String end);
  
  List<StringComparisonTestDoc> findBySearchableStringFieldGreaterThan(String value);
  
  List<StringComparisonTestDoc> findByTextIndexedStringFieldGreaterThan(String value);
}