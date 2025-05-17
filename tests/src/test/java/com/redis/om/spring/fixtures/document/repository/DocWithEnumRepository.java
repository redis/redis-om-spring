package com.redis.om.spring.fixtures.document.repository;

import java.util.List;

import com.redis.om.spring.fixtures.document.model.DocWithEnum;
import com.redis.om.spring.fixtures.document.model.MyJavaEnum;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface DocWithEnumRepository extends RedisDocumentRepository<DocWithEnum, String> {
  List<DocWithEnum> findByEnumProp(MyJavaEnum value);
}
