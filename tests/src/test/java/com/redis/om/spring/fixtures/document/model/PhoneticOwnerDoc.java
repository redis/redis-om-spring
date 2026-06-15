package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Searchable;

@Document
public class PhoneticOwnerDoc {
  @Searchable(
      phonetic = "dm:en"
  )
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
