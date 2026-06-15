package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Reference;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

@Document
public class PhoneticReferenceDoc {
  @Indexed
  private String filingDate;

  @Reference
  @Indexed
  private PhoneticOwnerDoc owner;

  public String getFilingDate() {
    return filingDate;
  }

  public void setFilingDate(String filingDate) {
    this.filingDate = filingDate;
  }

  public PhoneticOwnerDoc getOwner() {
    return owner;
  }

  public void setOwner(PhoneticOwnerDoc owner) {
    this.owner = owner;
  }
}
