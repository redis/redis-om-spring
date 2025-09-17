/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.converters;

import java.util.UUID;

public class UuidConverter implements Converter<UUID> {
  @Override
  public UUID parse(String s) {
    return UUID.fromString(s);
  }

  @Override
  public String toRedisString(UUID o) {
    return o.toString();
  }
}
