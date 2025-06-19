/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.converters;

public class IntegerConverter implements Converter<Integer> {
  @Override
  public Integer parse(String s) {
    return Integer.parseInt(s);
  }

  @Override
  public String toRedisString(Integer o) {
    return o.toString();
  }
}
