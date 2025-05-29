package com.redis.om.spring.convert;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * Converter for writing LocalDateTime values to Redis as strings.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to serialize LocalDateTime values when storing them in Redis as string values. The conversion
 * strategy converts the LocalDateTime to milliseconds since the Unix epoch using the system default
 * time zone and formats it as a string representation.
 * </p>
 * <p>
 * This converter is typically used for JSON document storage where LocalDateTime values need
 * to be represented as strings rather than binary data. The converter is automatically
 * registered with Spring Data Redis when Redis OM Spring is configured.
 * </p>
 *
 * @see LocalDateTimeToBytesConverter
 * @see org.springframework.data.convert.WritingConverter
 * @since 0.1.0
 */
@Component
@WritingConverter
public class LocalDateTimeToStringConverter implements Converter<LocalDateTime, String> {

  /**
   * Default constructor.
   * Creates a new instance of LocalDateTimeToStringConverter for converting LocalDateTime to strings.
   */
  public LocalDateTimeToStringConverter() {
    // Default constructor
  }

  /**
   * Converts a LocalDateTime value to its string representation.
   * <p>
   * The conversion process:
   * <ol>
   * <li>Converts the LocalDateTime to a ZonedDateTime using the system default time zone</li>
   * <li>Converts to an Instant and extracts milliseconds since the Unix epoch</li>
   * <li>Converts the milliseconds to a string</li>
   * </ol>
   *
   * @param source the LocalDateTime to convert
   * @return the string representation of the timestamp in milliseconds
   */
  @Override
  public String convert(LocalDateTime source) {
    Instant instant = ZonedDateTime.of(source, ZoneId.systemDefault()).toInstant();
    long timeInMillis = instant.toEpochMilli();
    return Long.toString(timeInMillis);
  }
}