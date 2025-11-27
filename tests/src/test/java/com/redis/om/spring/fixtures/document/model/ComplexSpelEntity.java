package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.IndexCreationMode;
import com.redis.om.spring.annotations.IndexingOptions;

/**
 * Test entity with complex SpEL expression for conditional index naming.
 * Uses environment property to determine index name based on environment.
 */
@Document
@IndexingOptions(
    indexName = "#{@environment.getProperty('app.environment') == 'production' ? 'prod_complex_idx' : 'dev_complex_idx'}",
    creationMode = IndexCreationMode.DROP_AND_RECREATE
)
public class ComplexSpelEntity {
  @Id
  private String id;
  private String data;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
