package com.redis.om.spring.fixtures.document.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;

import lombok.Data;

@Document
@Data
public class DocumentProjectionPojo {

  @Id
  private String id;

  private String name;

  private String test;

  public DocumentProjectionPojo() {
    this.id = UUID.randomUUID().toString();
  }

}
