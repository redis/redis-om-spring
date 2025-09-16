package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.RxDocument;
import com.redis.om.spring.repository.RedisDocumentRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RxDocumentRepository extends RedisDocumentRepository<RxDocument, String> {
}