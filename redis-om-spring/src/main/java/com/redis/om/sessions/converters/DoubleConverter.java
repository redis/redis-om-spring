/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.converters;

public class DoubleConverter implements Converter<Double> {
  @Override
  public Double parse(String s) {
    return Double.parseDouble(s);
  }

  @Override
  public String toRedisString(Double o) {
    return o.toString();
  }
}
