/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.converters;

public class LongConverter implements Converter<Long> {
  @Override
  public Long parse(String s) {
    return Long.parseLong(s);
  }

  @Override
  public String toRedisString(Long o) {
    return o.toString();
  }
}
