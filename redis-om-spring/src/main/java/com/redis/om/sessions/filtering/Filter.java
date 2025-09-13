/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.filtering;

public abstract class Filter {
  public abstract String getQuery();

  public CompositeFilter and() {
    return new CompositeFilter(this, new AndFilter());
  }

  public CompositeFilter or() {
    return new CompositeFilter(this, new OrFilter());
  }
}
