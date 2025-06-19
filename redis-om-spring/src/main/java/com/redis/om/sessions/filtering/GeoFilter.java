/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.filtering;

import com.redis.om.sessions.GeoLoc;
import com.redis.om.sessions.GeoUnit;

public class GeoFilter extends Filter {
  private final GeoLoc geoLoc;
  private final String fieldName;
  private final double radius;
  private final GeoUnit geoUnit;

  public GeoFilter(String fieldName, GeoLoc geoLoc, double radius, GeoUnit geoUnit) {
    this.geoLoc = geoLoc;
    this.fieldName = fieldName;
    this.radius = radius;
    this.geoUnit = geoUnit;
  }

  @Override
  public String getQuery() {
    return String.format("@%s:[%s %s %s %s]", fieldName, geoLoc.getLongitude(), geoLoc.getLatitude(), radius, geoUnit
        .name());
  }
}
