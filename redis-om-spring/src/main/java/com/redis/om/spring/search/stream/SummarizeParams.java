package com.redis.om.spring.search.stream;

public class SummarizeParams {
  private Integer fragsNum = 3;
  private Integer fragSize = 20;
  private String separator = "...";

  public static SummarizeParams instance() {
    return new SummarizeParams();
  }

  public Integer getFragsNum() {
    return fragsNum;
  }

  public Integer getFragSize() {
    return fragSize;
  }

  public String getSeparator() {
    return separator;
  }

  public SummarizeParams fragments(int num) {
    this.fragsNum = num;
    return this;
  }

  public SummarizeParams size(int size) {
    this.fragSize = size;
    return this;
  }

  public SummarizeParams separator(String separator) {
    this.separator = separator;
    return this;
  }
}
