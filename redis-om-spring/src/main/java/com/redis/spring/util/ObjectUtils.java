package com.redis.spring.util;

import org.springframework.data.geo.Distance;

public class ObjectUtils {
  public static String getDistanceAsRedisString(Distance distance) {
    return String.format("%s %s", Double.toString(distance.getValue()), distance.getUnit());
  }
}
