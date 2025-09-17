/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.converters;

public class ShortConverter implements Converter<Short> {
  @Override
  public Short parse(String s) {
    return Short.parseShort(s);
  }

  @Override
  public String toRedisString(Short o) {
    return o.toString();
  }
}
