package com.redislabs.spring.repository.support;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.redislabs.spring.mapping.RedisDocumentPersistentEntity;
import com.redislabs.spring.mapping.RedisDocumentPersistentProperty;
import com.redislabs.spring.ops.json.JSONOperations;
import com.redislabs.spring.repository.impl.RedisDocumentRepositoryImpl;

public class RedisDocumentRepositoryFactory extends RepositoryFactorySupport {
 
  private final JSONOperations<?> operations;
  private final MappingContext<? extends RedisDocumentPersistentEntity<?>, RedisDocumentPersistentProperty> mappingContext;
  
  public RedisDocumentRepositoryFactory(JSONOperations<?> operations) {
    Assert.notNull(operations, "JSONOperations must not be null!");
    this.operations = operations;
    this.mappingContext = null;
  }
  
  @Override
  public <T, ID> RedisDocumentEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
    

    /*
    
        MongoPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(domainClass);
    return MongoEntityInformationSupport.<T, ID> entityInformationFor(entity,
        metadata != null ? metadata.getIdType() : null);
        
          @SuppressWarnings("unchecked")
  static <T, ID> MongoEntityInformation<T, ID> entityInformationFor(MongoPersistentEntity<?> entity,
      @Nullable Class<?> idType) {

    Assert.notNull(entity, "Entity must not be null!");

    return new MappingMongoEntityInformation<>((MongoPersistentEntity<T>) entity, (Class<ID>) idType);
  }
  
  
     */
    return getEntityInformation(domainClass, null);
  }
  
  private <T, ID> RedisDocumentEntityInformation<T, ID> getEntityInformation(Class<T> domainClass, @Nullable RepositoryMetadata metadata) {
    RedisDocumentPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(domainClass);
    return RedisDocumentEntityInformationSupport.<T, ID> entityInformationFor(entity,
        metadata != null ? metadata.getIdType() : null);
  }

  @Override
  protected Object getTargetRepository(RepositoryInformation metadata) {
    return instantiateClass(getRepositoryBaseClass(metadata));
  }

  @Override
  protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
    return RedisDocumentRepositoryImpl.class;
  }

}
