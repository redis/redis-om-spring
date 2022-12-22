package com.redis.om.spring.repository.query.autocomplete;

import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
public class AutoCompleteOptions {
  private boolean fuzzy = false;
  private int limit = 5;
  private boolean withScore = false;
  private boolean withPayload = false;

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

  public boolean isWithScore() {
    return withScore;
  }
  public boolean isWithPayload() {
    return withPayload;
  }

  public int getLimit() {
    return limit;
  }
}
