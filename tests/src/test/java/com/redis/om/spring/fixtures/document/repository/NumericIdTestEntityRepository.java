package com.redis.om.spring.fixtures.document.repository;

import java.util.Collection;
import java.util.List;

import com.redis.om.spring.fixtures.document.model.NumericIdTestEntity;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface NumericIdTestEntityRepository extends RedisDocumentRepository<NumericIdTestEntity, Long> {
    // Test querying by numeric ID
    List<NumericIdTestEntity> findByIdIn(Collection<Long> ids);
    List<NumericIdTestEntity> findByIdNotIn(Collection<Long> ids);
    
    // Combined queries with numeric ID
    List<NumericIdTestEntity> findByIdInAndName(Collection<Long> ids, String name);
    List<NumericIdTestEntity> findByValueIn(Collection<Integer> values);
}