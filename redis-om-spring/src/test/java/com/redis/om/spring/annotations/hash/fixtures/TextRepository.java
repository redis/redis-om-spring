package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

public interface TextRepository extends RedisDocumentRepository<Text, String> {
}
