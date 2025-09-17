/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.filtering;

public class OrFilter extends LogicalFilter {
  @Override
  public String getQuery() {
    return " | ";
  }
}
