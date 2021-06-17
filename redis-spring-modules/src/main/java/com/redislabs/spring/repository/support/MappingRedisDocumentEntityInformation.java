package com.redislabs.spring.repository.support;

import org.springframework.data.repository.core.support.PersistentEntityInformation;
import org.springframework.lang.Nullable;

import com.redislabs.spring.mapping.RedisDocumentPersistentEntity;

public class MappingRedisDocumentEntityInformation<T, ID> extends PersistentEntityInformation<T, ID>
    implements RedisDocumentEntityInformation<T, ID> {
  
  private final RedisDocumentPersistentEntity<T> entityMetadata;
  private final @Nullable String customCollectionName;
  private final Class<ID> fallbackIdType;
  
  public MappingRedisDocumentEntityInformation(RedisDocumentPersistentEntity<T> entity, @Nullable Class<ID> idType) {
    this(entity, null, idType);
  }
  
  public MappingRedisDocumentEntityInformation(RedisDocumentPersistentEntity<T> entity, String customCollectionName, @Nullable Class<ID> idType) {
    super(entity);

    this.entityMetadata = entity;
    this.customCollectionName = customCollectionName;
    this.fallbackIdType = idType != null ? idType : (Class<ID>)null;
  }

  @Override
  public String getCollectionName() {
    return customCollectionName == null ? entityMetadata.getCollection() : customCollectionName;
  }

  @Override
  public String getIdAttribute() {
    return entityMetadata.getRequiredIdProperty().getName();
  }

}
