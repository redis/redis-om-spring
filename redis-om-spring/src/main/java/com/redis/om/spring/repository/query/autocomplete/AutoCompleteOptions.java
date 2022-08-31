package com.redis.om.spring.repository.query.autocomplete;

import io.redisearch.client.SuggestionOptions;
import io.redisearch.client.SuggestionOptions.With;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
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
    if (Boolean.TRUE.equals(fuzzy))
      builder = builder.fuzzy();
    if (Boolean.TRUE.equals(withPayload))
      builder = builder.with(With.PAYLOAD);
    if (Boolean.TRUE.equals(withScore))
      builder = builder.with(With.SCORES);
    if (limit != null)
      builder = builder.max(limit);

    return builder.build();
  }
}
