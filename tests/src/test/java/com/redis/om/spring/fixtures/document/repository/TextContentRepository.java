package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.TextContent;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface TextContentRepository extends RedisDocumentRepository<TextContent, String> {
}