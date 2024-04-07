package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.repository.RedisEnhancedRepository;

import java.util.List;
import java.util.Optional;

public interface Hash4Repository extends RedisEnhancedRepository<Hash4, String> {
  Optional<Hash4> findOneByFirstAndSecondNull(String first);

  Optional<Hash4> findOneBySecondNull();

  List<Hash4> findByFirstAndSecondNull(String first);

  List<Hash4> findByFirstAndSecondNotNull(String first);
}
