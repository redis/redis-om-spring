/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.filtering;

public class GreaterThanFilter<L extends Number> extends Filter {
  private final String fieldName;
  private final L lowerBound;

  public GreaterThanFilter(String fieldName, L lowerBound) {
    this.fieldName = fieldName;
    this.lowerBound = lowerBound;
  }

  @Override
  public String getQuery() {
    return String.format("@%s:[%s +inf]", fieldName, lowerBound.toString());
  }
}
