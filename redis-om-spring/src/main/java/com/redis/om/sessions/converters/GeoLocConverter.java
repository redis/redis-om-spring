/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.converters;

import com.redis.om.sessions.GeoLoc;

public class GeoLocConverter implements Converter<GeoLoc> {
  @Override
  public GeoLoc parse(String s) {
    return GeoLoc.parse(s);
  }

  @Override
  public String toRedisString(GeoLoc o) {
    return o.toString();
  }
}
