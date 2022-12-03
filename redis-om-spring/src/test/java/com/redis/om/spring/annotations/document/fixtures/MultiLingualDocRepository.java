package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

import com.redis.om.spring.repository.query.SearchLanguage;
import io.redisearch.SearchResult;

public interface MultiLingualDocRepository extends RedisDocumentRepository< MultiLingualDoc, String> {

  SearchResult findByBody(String text, SearchLanguage language);

}
