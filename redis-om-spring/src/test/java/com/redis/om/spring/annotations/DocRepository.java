package com.redis.om.spring.annotations;

import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DocRepository extends RedisDocumentRepository<Doc, String> {
}
