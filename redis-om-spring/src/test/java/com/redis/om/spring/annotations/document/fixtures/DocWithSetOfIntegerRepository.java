package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Set;

public interface DocWithSetOfIntegerRepository extends RedisDocumentRepository<DocWithSetOfInteger,String> {
  Iterable<DocWithSetOfInteger> findByTheNumbersContaining(Set<Integer> ints);
  Iterable<DocWithSetOfInteger> findByTheNumbersContainingAll(Set<Integer> ints);
}
