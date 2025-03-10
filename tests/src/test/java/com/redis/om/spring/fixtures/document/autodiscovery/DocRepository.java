package com.redis.om.spring.fixtures.document.autodiscovery;

import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DocRepository extends RedisDocumentRepository<Doc, String> {
}
