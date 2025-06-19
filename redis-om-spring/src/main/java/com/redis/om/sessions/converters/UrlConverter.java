/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.converters;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlConverter implements Converter<URL> {

  @Override
  public URL parse(String s) {
    try {
      return new URL(s);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toRedisString(URL o) {
    return o.toString();
  }
}
