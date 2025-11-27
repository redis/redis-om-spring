package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.IndexCreationMode;
import com.redis.om.spring.annotations.IndexingOptions;

/**
 * Test entity that uses System properties in SpEL expression for index naming.
 * Demonstrates dynamic index name resolution using JVM system properties.
 */
@Document
@IndexingOptions(
    indexName = "system_idx_#{T(java.lang.System).getProperty('custom.index.suffix')}",
    creationMode = IndexCreationMode.DROP_AND_RECREATE
)
public class SystemPropertyEntity {
  @Id
  private String id;
  private String content;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
