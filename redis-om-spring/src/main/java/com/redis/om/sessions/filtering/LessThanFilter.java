/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.filtering;

public class LessThanFilter<U extends Number> extends Filter {
  private final String fieldName;
  private final U upperBound;

  public LessThanFilter(String fieldName, U upperBound) {
    this.fieldName = fieldName;
    this.upperBound = upperBound;
  }

  @Override
  public String getQuery() {
    return String.format("@%s:[-inf %s]", this.fieldName, this.upperBound.toString());
  }
}
