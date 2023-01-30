package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;
import redis.clients.jedis.search.SearchResult;

@SuppressWarnings({ "unused", "SpringDataRepositoryMethodReturnTypeInspection" }) public interface SpanishDocRepository extends RedisDocumentRepository<SpanishDoc, String> {

  SearchResult findByBody(String text);

}
