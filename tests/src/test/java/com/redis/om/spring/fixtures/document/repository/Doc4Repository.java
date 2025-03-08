package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Doc4;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;
import java.util.Optional;

public interface Doc4Repository extends RedisDocumentRepository<Doc4, String> {
  Optional<Doc4> findOneByFirstAndSecondNull(String first);

  Optional<Doc4> findOneBySecondNull();

  List<Doc4> findByFirstAndSecondNull(String first);

  List<Doc4> findByFirstAndSecondNotNull(String first);
}
