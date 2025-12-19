/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.converters;

import java.net.URI;

public class URIConverter implements Converter<URI> {
  @Override
  public URI parse(String s) {
    return URI.create(s);
  }

  @Override
  public String toRedisString(URI o) {
    return o.toString();
  }
}
