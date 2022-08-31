package com.redis.om.spring.annotations.hash.fixtures;

import java.util.Optional;

import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface WithAliasRepository extends RedisEnhancedRepository<WithAlias, String> {
  Optional<WithAlias> findFirstByNumber(Integer number);
}
