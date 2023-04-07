package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;

@SuppressWarnings("unused") public interface DocWithEnumRepository extends RedisDocumentRepository<DocWithEnum, String> {
  List<DocWithEnum> findByEnumProp(MyJavaEnum value);
}
