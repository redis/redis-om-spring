/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.converters;

public class EnumConverter implements Converter<Enum> {
  private final Class clazz;

  public EnumConverter(Class clazz) {
    if (!Enum.class.isAssignableFrom(clazz)) {
      throw new IllegalArgumentException(String.format("Attempted to initialize enum converter from non-enum type: %s",
          clazz.getName()));
    }
    this.clazz = clazz;
  }

  @Override
  public Enum parse(String s) {
    return Enum.valueOf(clazz, s);
  }

  @Override
  public String toRedisString(Enum o) {
    return o.name();
  }
}
