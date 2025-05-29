package com.redis.om.spring.convert;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * Converter for writing OffsetDateTime values to Redis as strings.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to serialize OffsetDateTime values when storing them in Redis as string values. The conversion
 * strategy converts the OffsetDateTime to seconds since the Unix epoch using the system default
 * time zone and formats it as a string representation.
 * </p>
 * <p>
 * This converter is typically used for JSON document storage where OffsetDateTime values need
 * to be represented as strings rather than binary data. The converter is automatically
 * registered with Spring Data Redis when Redis OM Spring is configured.
 * </p>
 *
 * @see OffsetDateTimeToBytesConverter
 * @see org.springframework.data.convert.WritingConverter
 * @since 0.1.0
 */
@Component
@WritingConverter
public class OffsetDateTimeToStringConverter implements Converter<OffsetDateTime, String> {

  /**
   * Default constructor.
   */
  public OffsetDateTimeToStringConverter() {
  }

  /**
   * Converts an OffsetDateTime value to its string representation.
   * <p>
   * The conversion process:
   * <ol>
   * <li>Converts the OffsetDateTime to the same instant in the system default time zone</li>
   * <li>Extracts seconds since the Unix epoch</li>
   * <li>Converts the seconds to a string</li>
   * </ol>
   *
   * @param source the OffsetDateTime to convert
   * @return the string representation of the timestamp in seconds
   */
  @Override
  public String convert(OffsetDateTime source) {
    Instant instant = source.atZoneSameInstant(ZoneId.systemDefault()).toInstant();
    long unixTime = instant.getEpochSecond();
    return Long.toString(unixTime);
  }
}
