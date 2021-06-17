package com.redislabs.spring.repository.support;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.redislabs.spring.mapping.RedisDocumentPersistentEntity;

public final class RedisDocumentEntityInformationSupport {
  private RedisDocumentEntityInformationSupport() {}

  @SuppressWarnings("unchecked")
  static <T, ID> RedisDocumentEntityInformation<T, ID> entityInformationFor(RedisDocumentPersistentEntity<?> entity,
      @Nullable Class<?> idType) {
    Assert.notNull(entity, "Entity must not be null!");
    return new MappingRedisDocumentEntityInformation<T, ID>((RedisDocumentPersistentEntity<T>) entity, (Class<ID>) idType);
  }
}
