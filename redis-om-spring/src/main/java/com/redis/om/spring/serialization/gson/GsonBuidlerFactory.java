package com.redis.om.spring.serialization.gson;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.data.geo.Point;

import com.google.gson.GsonBuilder;

public class GsonBuidlerFactory {
  private static GsonBuilder builder = new GsonBuilder();
  static {
    builder.registerTypeAdapter(Point.class, PointTypeAdapter.getInstance());
    builder.registerTypeAdapter(Date.class, DateTypeAdapter.getInstance());
    builder.registerTypeAdapter(LocalDate.class, LocalDateTypeAdapter.getInstance());
    builder.registerTypeAdapter(LocalDateTime.class, LocalDateTimeTypeAdapter.getInstance());
  }
  
  public static GsonBuilder getBuilder() {
    return builder;
  }
}
