package com.redis.om.spring.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.redis.core.convert.RedisCustomConversions;

/**
 * Custom conversions configuration for Redis OM Spring framework.
 * <p>
 * This class extends Spring Data Redis's {@link RedisCustomConversions} to provide
 * additional type converters specifically needed for Redis OM Spring functionality.
 * <p>
 * The following additional converters are registered:
 * <ul>
 * <li>ULID converters for universally unique lexicographically sortable identifiers</li>
 * <li>Point converters for geospatial coordinates</li>
 * <li>Enhanced date/time converters for various temporal types</li>
 * <li>Boolean converters for proper boolean handling</li>
 * <li>YearMonth converters for date period handling</li>
 * </ul>
 *
 * @since 1.0.0
 */
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
    omConverters.add(new LocalDateTimeToStringConverter());

    // Boolean
    omConverters.add(new BooleanToBytesConverter());
    omConverters.add(new BytesToBooleanConverter());

    // YearMonth
    omConverters.add(new YearMonthToBytesConverter());
    omConverters.add(new YearMonthToStringConverter());
    omConverters.add(new BytesToYearMonthConverter());
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
