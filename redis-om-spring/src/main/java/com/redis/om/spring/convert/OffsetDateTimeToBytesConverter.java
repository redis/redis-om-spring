package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * Converter for writing OffsetDateTime values to Redis as byte arrays.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to serialize OffsetDateTime values when storing them in Redis. The conversion strategy
 * converts the OffsetDateTime to milliseconds since the Unix epoch using the system default
 * time zone, then encodes that as a UTF-8 string in byte array format.
 * </p>
 * <p>
 * The converter is automatically registered with Spring Data Redis when Redis OM Spring
 * is configured and is used transparently during entity serialization operations.
 * </p>
 *
 * @see BytesToOffsetDateTimeConverter
 * @see org.springframework.data.convert.WritingConverter
 * @since 0.1.0
 */
@Component
@WritingConverter
public class OffsetDateTimeToBytesConverter implements Converter<OffsetDateTime, byte[]> {

  /**
   * Default constructor for OffsetDateTimeToBytesConverter.
   * <p>
   * This constructor is used by Spring's dependency injection framework
   * to instantiate the converter as a component.
   * </p>
   */
  public OffsetDateTimeToBytesConverter() {
    // Default constructor for Spring component instantiation
  }

  /**
   * Converts a string to its UTF-8 byte array representation.
   *
   * @param source the string to convert
   * @return the UTF-8 encoded byte array
   */
  byte[] fromString(String source) {
    return source.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Converts an OffsetDateTime value to a byte array representation.
   * <p>
   * The conversion process:
   * <ol>
   * <li>Converts the OffsetDateTime to the same instant in the system default time zone</li>
   * <li>Extracts milliseconds since the Unix epoch</li>
   * <li>Converts the milliseconds to a string</li>
   * <li>Encodes the string as UTF-8 bytes</li>
   * </ol>
   *
   * @param source the OffsetDateTime to convert
   * @return the byte array representation containing the timestamp as a UTF-8 string
   */
  @Override
  public byte[] convert(OffsetDateTime source) {
    Instant instant = source.atZoneSameInstant(ZoneId.systemDefault()).toInstant();
    long timeInMillis = instant.toEpochMilli();

    return fromString(Long.toString(timeInMillis));
  }
}
