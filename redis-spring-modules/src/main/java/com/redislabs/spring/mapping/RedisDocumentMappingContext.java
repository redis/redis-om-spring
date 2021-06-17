package com.redislabs.spring.mapping;

import org.springframework.context.ApplicationContextAware;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

public class RedisDocumentMappingContext extends AbstractMappingContext<RedisDocumentPersistentEntity<?>, RedisDocumentPersistentProperty>
implements ApplicationContextAware {

  @Override
  protected <T> RedisDocumentPersistentEntity<?> createPersistentEntity(TypeInformation<T> typeInformation) {
    return new BasicRedisDocumentPersistentEntity<>(typeInformation);
  }

  @Override
  protected RedisDocumentPersistentProperty createPersistentProperty(Property property,
      RedisDocumentPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
    // TODO Auto-generated method stub
    return null;
  }

}
