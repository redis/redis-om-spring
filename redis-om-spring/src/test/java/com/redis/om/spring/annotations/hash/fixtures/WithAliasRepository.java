package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.repository.RedisEnhancedRepository;

import java.util.Optional;

@SuppressWarnings("unused") public interface WithAliasRepository extends RedisEnhancedRepository<WithAlias, String> {
  Optional<WithAlias> findFirstByNumber(Integer number);
}
