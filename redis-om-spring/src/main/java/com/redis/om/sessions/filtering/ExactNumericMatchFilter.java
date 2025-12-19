/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.filtering;

public class ExactNumericMatchFilter<T extends Number> extends Filter {
  T fieldValue;
  String fieldName;

  public ExactNumericMatchFilter(String fieldName, T fieldValue) {
    this.fieldName = fieldName;
    this.fieldValue = fieldValue;
  }

  @Override
  public String getQuery() {
    return String.format("@%s:[%s %s]", this.fieldName, this.fieldValue, this.fieldValue);
  }
}
