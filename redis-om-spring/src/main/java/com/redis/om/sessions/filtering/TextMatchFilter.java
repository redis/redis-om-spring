/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.filtering;

public class TextMatchFilter extends Filter {
  private final String fieldName;
  private final String fieldValue;

  public TextMatchFilter(String fieldName, String fieldValue) {
    this.fieldName = fieldName;
    this.fieldValue = fieldValue;
  }

  @Override
  public String getQuery() {
    return String.format("@%s:%s", fieldName, fieldValue);
  }
}
