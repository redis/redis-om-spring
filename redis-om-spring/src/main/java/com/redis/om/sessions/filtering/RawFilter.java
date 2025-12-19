/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.filtering;

public class RawFilter extends Filter {
  private final String predicate;

  public RawFilter(String predicate) {
    this.predicate = predicate;
  }

  @Override
  public String getQuery() {
    return predicate;
  }
}
