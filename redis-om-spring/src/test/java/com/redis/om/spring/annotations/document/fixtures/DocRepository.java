package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings("unused") public interface DocRepository extends RedisDocumentRepository<Doc, String> {
}
