package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.DocWithEnum;
import com.redis.om.spring.fixtures.document.model.MyJavaEnum;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;

@SuppressWarnings("unused")
public interface DocWithEnumRepository extends RedisDocumentRepository<DocWithEnum, String> {
  List<DocWithEnum> findByEnumProp(MyJavaEnum value);
}
