package com.redis.om.spring.annotations;

import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings("unused") public interface DocRepository extends RedisDocumentRepository<Doc, String> {
}
