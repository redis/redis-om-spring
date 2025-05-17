package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.MultiLingualDoc;
import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redis.om.spring.repository.query.SearchLanguage;

import redis.clients.jedis.search.SearchResult;

@SuppressWarnings(
  { "unused", "SpringDataRepositoryMethodReturnTypeInspection" }
)
public interface MultiLingualDocRepository extends RedisDocumentRepository<MultiLingualDoc, String> {

  SearchResult findByBody(String text, SearchLanguage language);

}
