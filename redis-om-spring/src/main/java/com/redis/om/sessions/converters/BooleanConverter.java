/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.converters;

public class BooleanConverter implements Converter<Boolean> {
  @Override
  public Boolean parse(String s) {
    return Boolean.parseBoolean(s);
  }

  @Override
  public String toRedisString(Boolean o) {
    return o.toString();
  }
}
