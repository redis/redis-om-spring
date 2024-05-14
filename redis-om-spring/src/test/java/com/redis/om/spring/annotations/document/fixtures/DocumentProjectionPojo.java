package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.UUID;

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
