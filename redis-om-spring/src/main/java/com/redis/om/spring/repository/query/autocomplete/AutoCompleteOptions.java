package com.redis.om.spring.repository.query.autocomplete;

public class AutoCompleteOptions {
  private boolean fuzzy = false;
  private int limit = 5;
  private boolean withScore = false;
  private boolean withPayload = false;

  public AutoCompleteOptions() {
  }

  public static AutoCompleteOptions get() {
    return new AutoCompleteOptions();
  }

  public AutoCompleteOptions withPayload() {
    setWithPayload(true);
    return this;
  }

  public AutoCompleteOptions withScore() {
    setWithScore(true);
    return this;
  }

  public AutoCompleteOptions limit(Integer limit) {
    setLimit(limit);
    return this;
  }

  public AutoCompleteOptions fuzzy() {
    setFuzzy(true);
    return this;
  }

  public boolean isFuzzy() {
    return fuzzy;
  }

  public void setFuzzy(boolean fuzzy) {
    this.fuzzy = fuzzy;
  }

  public boolean isWithScore() {
    return withScore;
  }

  public void setWithScore(boolean withScore) {
    this.withScore = withScore;
  }

  public boolean isWithPayload() {
    return withPayload;
  }

  public void setWithPayload(boolean withPayload) {
    this.withPayload = withPayload;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }
}
