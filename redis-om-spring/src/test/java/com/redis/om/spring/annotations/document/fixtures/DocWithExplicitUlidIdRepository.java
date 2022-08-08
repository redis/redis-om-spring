package com.redis.om.spring.annotations.document.fixtures;

import com.github.f4b6a3.ulid.Ulid;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface DocWithExplicitUlidIdRepository extends RedisDocumentRepository<DocWithExplicitUlidId, Ulid> {
}
