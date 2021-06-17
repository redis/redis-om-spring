package com.redislabs.spring.mapping;

import org.springframework.data.mapping.model.MutablePersistentEntity;

public interface RedisDocumentPersistentEntity<T> extends MutablePersistentEntity<T, RedisDocumentPersistentProperty> {

  String getCollection();

}
