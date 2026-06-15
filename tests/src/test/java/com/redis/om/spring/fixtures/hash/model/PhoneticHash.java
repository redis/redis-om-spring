package com.redis.om.spring.fixtures.hash.model;

import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

@RedisHash
public class PhoneticHash {
  @Indexed
  private String filingDate;

  @Searchable(
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
