/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import lombok.Getter;

@Getter
public class GeoLoc {
  private final double latitude;
  private final double longitude;

  public GeoLoc(double longitude, double latitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  @Override
  public String toString() {
    return String.format("%f,%f", this.longitude, this.latitude);
  }

  public static GeoLoc parse(String s) {
    String[] parts = s.split(",");
    if (parts.length != 2) {
      throw new IllegalArgumentException(String.format("unparseable point %s", s));
    }

    return new GeoLoc(Double.parseDouble(parts[1]), Double.parseDouble(parts[1]));
  }
}
