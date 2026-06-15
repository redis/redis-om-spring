package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.TextIndexed;

@Document
public class PhoneticTextIndexedDoc {
  @Indexed
  private String filingDate;

  @TextIndexed(
      phonetic = "dm:en"
  )
  private String chunkText;

  public String getFilingDate() {
    return filingDate;
  }

  public void setFilingDate(String filingDate) {
    this.filingDate = filingDate;
  }

  public String getChunkText() {
    return chunkText;
  }

  public void setChunkText(String chunkText) {
    this.chunkText = chunkText;
  }
}
