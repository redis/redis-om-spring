package com.redis.om.spring.convert;

import org.springframework.data.redis.core.convert.RedisCustomConversions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RedisOMCustomConversions extends RedisCustomConversions {
  private static final List<Object> omConverters = new ArrayList<>();

  static {
    // Ulid
    omConverters.add(new UlidToBytesConverter());
    omConverters.add(new BytesToUlidConverter());
    // Point
    omConverters.add(new PointToBytesConverter());
    omConverters.add(new BytesToPointConverter());
    // Date
    omConverters.add(new DateToBytesConverter());
    omConverters.add(new BytesToDateConverter());
    omConverters.add(new DateToStringConverter());

    // LocalDate
    omConverters.add(new LocalDateToBytesConverter());
    omConverters.add(new BytesToLocalDateConverter());
    omConverters.add(new LocalDateToStringConverter());
    // OffsetDateTime
    omConverters.add(new OffsetDateTimeToBytesConverter());
    omConverters.add(new BytesToOffsetDateTimeConverter());
    omConverters.add(new OffsetDateTimeToStringConverter());
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
