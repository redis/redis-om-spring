package com.redis.om.spring.serialization.gson;

import org.springframework.data.geo.Point;

import com.google.gson.GsonBuilder;

public class GsonBuidlerFactory {
  public static GsonBuilder getBuilder() {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(Point.class, PointTypeAdapter.getInstance());
    return builder;
  }
}
