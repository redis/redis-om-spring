package com.redis.om.amr.entraid.repository;

import com.redis.om.amr.entraid.model.MyData;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface MyDataRepository extends RedisDocumentRepository<MyData, String> {
}
