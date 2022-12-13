package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

public interface SomeDocumentRepository extends RedisDocumentRepository<SomeDocument,String> {
}
