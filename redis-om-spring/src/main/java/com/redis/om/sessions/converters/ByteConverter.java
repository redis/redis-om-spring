/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.converters;

public class ByteConverter implements Converter<Byte> {
  @Override
  public Byte parse(String s) {
    return Byte.parseByte(s);
  }

  @Override
  public String toRedisString(Byte o) {
    return o.toString();
  }
}
