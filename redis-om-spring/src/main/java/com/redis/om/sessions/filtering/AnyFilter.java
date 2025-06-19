/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.filtering;

public class AnyFilter extends Filter {
  @Override
  public String getQuery() {
    return "*";
  }
}
