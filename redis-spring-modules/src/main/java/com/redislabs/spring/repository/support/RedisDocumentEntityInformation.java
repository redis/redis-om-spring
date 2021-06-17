package com.redislabs.spring.repository.support;

import org.springframework.data.repository.core.EntityInformation;

public interface RedisDocumentEntityInformation<T, ID> extends EntityInformation<T, ID> {
  /**
   * Returns the name of the collection the entity shall be persisted to.
   *
   * @return
   */
  String getCollectionName();

  /**
   * Returns the attribute that the id will be persisted to.
   *
   * @return
   */
  String getIdAttribute();
}
