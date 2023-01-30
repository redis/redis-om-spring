package com.redis.om.spring.autocomplete;

import java.util.HashMap;
import java.util.Map;

public class Suggestion {
  public static final String KEY_FORMAT_STRING = "sugg:%s:%s";
  public static final String PAYLOAD_KEY_FORMAT_STRING = "sugg:payload:%s:%s";
  private final String value;
  private double score = 1.0;
  private Map<String, Object> payload = new HashMap<>();

  public Suggestion(String value, double score, Map<String, Object> payload) {
    this.value = value;
    this.payload = payload;
    this.score = score;
  }

  public Suggestion(String value, double score) {
    this.value = value;
    this.score = score;
  }

  public Suggestion(String value, Map<String, Object> payload) {
    this.value = value;
    this.payload = payload;
  }

  public Suggestion(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public double getScore() {
    return score;
  }

  public Map<String, Object> getPayload() {
    return payload;
  }
}
