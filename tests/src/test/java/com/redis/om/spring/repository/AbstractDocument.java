package com.redis.om.spring.repository;

import java.util.UUID;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Indexed;

public abstract class AbstractDocument {
  @Id
  private String id;

  @Indexed
  private String inherited;

  protected AbstractDocument() {
    this.id = UUID.randomUUID().toString();
    this.inherited = "inherited";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getInherited() {
    return inherited;
  }

  public void setInherited(String inherited) {
    this.inherited = inherited;
  }
}
