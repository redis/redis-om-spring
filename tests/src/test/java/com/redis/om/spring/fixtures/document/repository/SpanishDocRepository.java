package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.SpanishDoc;
import com.redis.om.spring.repository.RedisDocumentRepository;
import redis.clients.jedis.search.SearchResult;

@SuppressWarnings({ "unused", "SpringDataRepositoryMethodReturnTypeInspection" })
public interface SpanishDocRepository extends RedisDocumentRepository<SpanishDoc, String> {

  SearchResult findByBody(String text);

}
