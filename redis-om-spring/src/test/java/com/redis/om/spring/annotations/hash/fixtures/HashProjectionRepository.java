package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.repository.RedisEnhancedRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Optional;

public interface HashProjectionRepository extends RedisEnhancedRepository<HashProjectionPojo, String> {
   Optional<HashProjection> findByName(String name);
   Collection<HashProjection> findAllByName(String name);
   Page<HashProjection> findAllByName(String name, Pageable pageable);
}
