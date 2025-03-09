package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.Hash4;
import com.redis.om.spring.repository.RedisEnhancedRepository;

import java.util.List;
import java.util.Optional;

public interface Hash4Repository extends RedisEnhancedRepository<Hash4, String> {
  Optional<Hash4> findOneByFirstAndSecondNull(String first);

  Optional<Hash4> findOneBySecondNull();

  List<Hash4> findByFirstAndSecondNull(String first);

  List<Hash4> findByFirstAndSecondNotNull(String first);
}
