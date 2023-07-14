package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.document.fixtures.Doc2;
import com.redis.om.spring.annotations.document.fixtures.Doc3;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings("unused") public interface Doc3Repository extends RedisDocumentRepository<Doc3, String> {
}
