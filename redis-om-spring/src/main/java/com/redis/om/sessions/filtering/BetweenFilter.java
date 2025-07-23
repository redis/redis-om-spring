/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.filtering;

public class BetweenFilter<L extends Number, U extends Number> extends Filter {
  private final String fieldName;
  private final L lowerBound;
  private final U upperBound;

  public BetweenFilter(String fieldName, L lowerBound, U upperBound) {
    this.fieldName = fieldName;
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  @Override
  public String getQuery() {
    return String.format("@%s:[%s %s]", fieldName, lowerBound.toString(), upperBound.toString());
  }
}
