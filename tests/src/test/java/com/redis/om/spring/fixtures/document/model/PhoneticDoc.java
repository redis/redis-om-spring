package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Searchable;

@Document
public class PhoneticDoc {
  @Searchable(
      phonetic = "dm:en"
  )
  private String phoneticName;

  @Searchable
  private String plainName;

  public String getPhoneticName() {
    return phoneticName;
  }

  public void setPhoneticName(String phoneticName) {
    this.phoneticName = phoneticName;
  }

  public String getPlainName() {
    return plainName;
  }

  public void setPlainName(String plainName) {
    this.plainName = plainName;
  }
}
