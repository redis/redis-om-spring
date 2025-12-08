package com.redis.om.spring.fixtures.document.repository;

import java.util.Collection;
import java.util.List;

import com.redis.om.spring.fixtures.document.model.AutoIndexedIdEntity;
import com.redis.om.spring.repository.RedisDocumentRepository;

/**
 * Repository for testing auto-indexed ID fields with findByIdIn queries.
 */
public interface AutoIndexedIdEntityRepository extends RedisDocumentRepository<AutoIndexedIdEntity, Long> {
    // Test querying by auto-indexed numeric ID (no explicit @NumericIndexed)
    List<AutoIndexedIdEntity> findByIdIn(Collection<Long> ids);
    List<AutoIndexedIdEntity> findByIdNotIn(Collection<Long> ids);

    // Combined queries
    List<AutoIndexedIdEntity> findByIdInAndName(Collection<Long> ids, String name);
}
