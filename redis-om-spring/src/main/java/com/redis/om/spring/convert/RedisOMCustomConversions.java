package com.redis.om.spring.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.redis.core.convert.RedisCustomConversions;

public class RedisOMCustomConversions extends RedisCustomConversions {
  private static final List<Object> omConverters = new ArrayList<>();
  
  static {
    // Ulid
    omConverters.add(new UlidToBytesConverter());
    omConverters.add(new BytesToUlidConverter());
    // Point
    omConverters.add(new PointToBytesConverter());
    omConverters.add(new BytesToPointConverter());
    // LocalDate
    omConverters.add(new LocalDateToBytesConverter());
    omConverters.add(new BytesToLocalDateConverter());
    omConverters.add(new LocalDateToStringConverter());
    // LocalDateTime
    omConverters.add(new LocalDateTimeToBytesConverter());
    omConverters.add(new BytesToLocalDateTimeConverter());
    // Boolean
    omConverters.add(new BooleanToBytesConverter());
    omConverters.add(new BytesToBooleanConverter());
  }

  /**
   * Creates an empty {@link RedisCustomConversions} object.
   */
  public RedisOMCustomConversions() {
    this(Collections.emptyList());
  }

  /**
   * Creates a new {@link RedisCustomConversions} instance registering the given converters.
   *
   * @param converters a list of converted to be added to the base list
   */
  public RedisOMCustomConversions(List<?> converters) {
    super(omConverters);
  }
}
