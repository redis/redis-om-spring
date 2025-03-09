package com.redis.om.spring.fixtures.document.repository;

import com.github.f4b6a3.ulid.Ulid;
import com.redis.om.spring.fixtures.document.model.DocWithExplicitUlidId;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings({ "unused", "SpellCheckingInspection" })
public interface DocWithExplicitUlidIdRepository extends RedisDocumentRepository<DocWithExplicitUlidId, Ulid> {
}
