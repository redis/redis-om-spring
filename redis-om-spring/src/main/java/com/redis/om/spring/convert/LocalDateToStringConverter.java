package com.redis.om.spring.convert;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * Converter for writing LocalDate values to Redis as strings.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to serialize LocalDate values when storing them in Redis as string values. The conversion
 * strategy converts the LocalDate to seconds since the Unix epoch using the system default
 * time zone (at start of day) and formats it as a string representation.
 * </p>
 * <p>
 * This converter is typically used for JSON document storage where LocalDate values need
 * to be represented as strings rather than binary data. The converter is automatically
 * registered with Spring Data Redis when Redis OM Spring is configured.
 * </p>
 *
 * @see LocalDateToBytesConverter
 * @see org.springframework.data.convert.WritingConverter
 * @since 0.1.0
 */
@Component
@WritingConverter
public class LocalDateToStringConverter implements Converter<LocalDate, String> {

  /**
   * Default constructor.
   * Creates a new instance of LocalDateToStringConverter for converting LocalDate to strings.
   */
  public LocalDateToStringConverter() {
    // Default constructor
  }

  /**
   * Converts a LocalDate value to its string representation.
   * <p>
   * The conversion process:
   * <ol>
   * <li>Converts the LocalDate to start of day using the system default time zone</li>
   * <li>Converts to an Instant and extracts seconds since the Unix epoch</li>
   * <li>Converts the seconds to a string</li>
   * </ol>
   *
   * @param source the LocalDate to convert
   * @return the string representation of the timestamp in seconds
   */
  @Override
  public String convert(LocalDate source) {
    Instant instant = source.atStartOfDay(ZoneId.systemDefault()).toInstant();
    long unixTime = instant.getEpochSecond();
    return Long.toString(unixTime);
  }
}
