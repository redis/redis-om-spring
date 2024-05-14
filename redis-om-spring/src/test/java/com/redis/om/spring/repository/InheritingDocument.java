package com.redis.om.spring.repository;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

@Document
public class InheritingDocument extends AbstractDocument {
  @Indexed
  private String notInherited;

  public InheritingDocument() {
    super();
    this.notInherited = "notInherited";
  }

  public String getNotInherited() {
    return notInherited;
  }

  public void setNotInherited(String notInherited) {
    this.notInherited = notInherited;
  }
}
