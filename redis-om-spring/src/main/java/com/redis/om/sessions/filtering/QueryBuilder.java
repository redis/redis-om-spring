/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.filtering;

import com.redis.om.sessions.GeoLoc;
import com.redis.om.sessions.GeoUnit;
import com.redis.om.sessions.converters.Converter;

public class QueryBuilder {
  public static AnyFilter any() {
    return new AnyFilter();
  }

  public static ExactStringMatchFilter equals(String fieldName, String fieldValue) {
    return new ExactStringMatchFilter(fieldName, fieldValue);
  }

  public static <T> ExactStringMatchFilter equals(String fieldName, T value, Converter<T> converter) {
    return new ExactStringMatchFilter(fieldName, converter.toRedisString(value));
  }

  public static TextMatchFilter textMatch(String fieldName, String matchValue) {
    return new TextMatchFilter(fieldName, matchValue);
  }

  public static <T extends Number> ExactNumericMatchFilter<T> equals(String fieldName, T fieldValue) {
    return new ExactNumericMatchFilter<>(fieldName, fieldValue);
  }

  public static <L extends Number, U extends Number> BetweenFilter<L, U> between(String fieldName, L lowerBound,
      U upperBound) {
    return new BetweenFilter<>(fieldName, lowerBound, upperBound);
  }

  public static <L extends Number> GreaterThanFilter<L> greaterThan(String fieldName, L lowerBound) {
    return new GreaterThanFilter<>(fieldName, lowerBound);
  }

  public static <U extends Number> LessThanFilter<U> lessThan(String fieldName, U upperBound) {
    return new LessThanFilter<>(fieldName, upperBound);
  }

  public static GeoFilter geoRadius(String fieldName, GeoLoc point, double distance, GeoUnit geoUnit) {
    return new GeoFilter(fieldName, point, distance, geoUnit);
  }
}
