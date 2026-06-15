package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

@Document
public class PhoneticMultiFieldDoc {
  @Indexed
  private String filingDate;

  @Indexed
  private String category;

  @Searchable(
      phonetic = "dm:en"
  )
  private String chunkText;

  @Searchable(
      phonetic = "dm:en"
  )
  private String customerName;

  public String getFilingDate() {
    return filingDate;
  }

  public void setFilingDate(String filingDate) {
    this.filingDate = filingDate;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getChunkText() {
    return chunkText;
  }

  public void setChunkText(String chunkText) {
    this.chunkText = chunkText;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }
}
