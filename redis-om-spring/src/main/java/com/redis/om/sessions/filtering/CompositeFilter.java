/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.filtering;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.redis.om.sessions.GeoLoc;
import com.redis.om.sessions.GeoUnit;
import com.redis.om.sessions.converters.Converter;

public class CompositeFilter extends Filter {
  private final List<Filter> filters = new ArrayList<>();

  CompositeFilter(Filter firstFilter, LogicalFilter logicalFilter) {
    filters.add(firstFilter);
    filters.add(logicalFilter);
  }

  public Filter equals(String fieldName, String fieldValue) {
    this.filters.add(new ExactStringMatchFilter(fieldName, fieldValue));
    return this;
  }

  public <T> Filter equals(String fieldName, T value, Converter<T> converter) {
    this.filters.add(new ExactStringMatchFilter(fieldName, converter.toRedisString(value)));
    return this;
  }

  public Filter textMatch(String fieldName, String matchValue) {
    this.filters.add(new TextMatchFilter(fieldName, matchValue));
    return this;
  }

  public <T extends Number> Filter equals(String fieldName, T fieldValue) {
    this.filters.add(new ExactNumericMatchFilter<>(fieldName, fieldValue));
    return this;
  }

  public <L extends Number, U extends Number> Filter between(String fieldName, L lowerBound, U upperBound) {
    this.filters.add(new BetweenFilter<>(fieldName, lowerBound, upperBound));
    return this;
  }

  public <L extends Number> Filter greaterThan(String fieldName, L lowerBound) {
    this.filters.add(new GreaterThanFilter<>(fieldName, lowerBound));
    return this;
  }

  public <U extends Number> Filter lessThan(String fieldName, U upperBound) {
    this.filters.add(new LessThanFilter<>(fieldName, upperBound));
    return this;
  }

  public Filter geoRadius(String fieldName, GeoLoc point, double distance, GeoUnit geoUnit) {
    this.filters.add(new GeoFilter(fieldName, point, distance, geoUnit));
    return this;
  }

  @Override
  public String getQuery() {
    StringBuilder sb = new StringBuilder();
    List<Filter> applicableFilters = filters.stream().filter(f -> !(f instanceof AnyFilter)).collect(Collectors
        .toList());
    if (applicableFilters.isEmpty()) { // all filters are AnyFilters, so we can just pass a * back
      return "*";
    }

    if (applicableFilters.size() == 2 && applicableFilters.get(0) instanceof LogicalFilter) { // handle case where first filter was any with a logical filter proceeding it.
      return applicableFilters.get(1).getQuery();
    }

    for (Filter filter : applicableFilters) {
      sb.append(filter.getQuery());
    }

    return String.format("(%s)", sb);
  }
}
