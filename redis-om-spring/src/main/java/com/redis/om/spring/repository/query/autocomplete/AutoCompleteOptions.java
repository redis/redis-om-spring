package com.redis.om.spring.repository.query.autocomplete;

import io.redisearch.client.SuggestionOptions;
import io.redisearch.client.SuggestionOptions.With;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoCompleteOptions {
  private Boolean fuzzy = false;
  private Integer limit = null;
  private Boolean withPayload = false;
  private Boolean withScore = false;
  
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
  
  public SuggestionOptions toSuggestionOptions() {
    SuggestionOptions.Builder builder = SuggestionOptions.builder();
    if (fuzzy) builder = builder.fuzzy();
    if (withPayload) builder = builder.with(With.PAYLOAD);
    if (withScore) builder = builder.with(With.SCORES);
    if (limit != null) builder = builder.max(limit);
    
    return builder.build();
  }
}
