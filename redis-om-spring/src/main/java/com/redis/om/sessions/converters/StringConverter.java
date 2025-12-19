/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.converters;

public class StringConverter implements Converter<String> {
  @Override
  public String parse(String s) {
    return s;
  }

  @Override
  public String toRedisString(String o) {
    return o;
  }
}
