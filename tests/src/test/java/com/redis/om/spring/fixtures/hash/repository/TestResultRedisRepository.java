package com.redis.om.spring.fixtures.hash.repository;

import java.util.List;

import com.redis.om.spring.annotations.Dialect;
import com.redis.om.spring.annotations.UseDialect;
import com.redis.om.spring.fixtures.hash.model.TestResultRedisModel;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface TestResultRedisRepository extends RedisEnhancedRepository<TestResultRedisModel, Long> {
  @UseDialect(
      dialect = Dialect.THREE
  )
  List<TestResultRedisModel> findAllByFilenameIs(String filename);
}