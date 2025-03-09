package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.KitchenSink;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings("unused")
public interface KitchenSinkRepository extends RedisDocumentRepository<KitchenSink, String> {

}
