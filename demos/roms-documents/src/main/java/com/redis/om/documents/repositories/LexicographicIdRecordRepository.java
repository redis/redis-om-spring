package com.redis.om.documents.repositories;

import com.redis.om.documents.domain.LexicographicIdRecord;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface LexicographicIdRecordRepository extends RedisDocumentRepository<LexicographicIdRecord, String> {
}
