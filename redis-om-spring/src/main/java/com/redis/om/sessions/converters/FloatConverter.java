/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.converters;

public class FloatConverter implements Converter<Float> {
  @Override
  public Float parse(String s) {
    return Float.parseFloat(s);
  }

  @Override
  public String toRedisString(Float o) {
    return o.toString();
  }
}
