package com.redis.om.spring.fixtures.document.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.document.model.TenantAwareEntity;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface TenantAwareEntityRepository extends RedisDocumentRepository<TenantAwareEntity, String> {

    Optional<TenantAwareEntity> findByName(String name);

    Iterable<TenantAwareEntity> findByTenantId(String tenantId);

    Iterable<TenantAwareEntity> findByNameContaining(String nameFragment);
}