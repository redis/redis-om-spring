package com.redis.om.spring.repository;

import java.util.UUID;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;

@Document
public class SimpleDocument {
  @Id
  private String id;

  public SimpleDocument() {
    this.id = UUID.randomUUID().toString();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
