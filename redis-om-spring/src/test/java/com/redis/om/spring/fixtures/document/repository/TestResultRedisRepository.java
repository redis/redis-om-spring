package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.annotations.UseDialect;
import com.redis.om.spring.annotations.Dialect;
import com.redis.om.spring.fixtures.document.model.TestResultRedisModel;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;

public interface TestResultRedisRepository extends RedisDocumentRepository<TestResultRedisModel, Long> {
  @UseDialect(dialect = Dialect.THREE)
  List<TestResultRedisModel> findAllByFilenameIs(String filename);
}