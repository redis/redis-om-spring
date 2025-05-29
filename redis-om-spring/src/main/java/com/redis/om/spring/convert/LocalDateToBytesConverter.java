package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * Converter for writing LocalDate values to Redis as byte arrays.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to serialize LocalDate values when storing them in Redis. The conversion strategy
 * converts the LocalDate to seconds since the Unix epoch using the system default
 * time zone (at start of day), then encodes that as a UTF-8 string in byte array format.
 * </p>
 * <p>
 * The converter is automatically registered with Spring Data Redis when Redis OM Spring
 * is configured and is used transparently during entity serialization operations.
 * </p>
 *
 * @see BytesToLocalDateConverter
 * @see org.springframework.data.convert.WritingConverter
 * @since 0.1.0
 */
@Component
@WritingConverter
public class LocalDateToBytesConverter implements Converter<LocalDate, byte[]> {

  /**
   * Default constructor.
   * Creates a new instance of LocalDateToBytesConverter for converting LocalDate to byte arrays.
   */
  public LocalDateToBytesConverter() {
    // Default constructor
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
   * Converts a LocalDate value to a byte array representation.
   * <p>
   * The conversion process:
   * <ol>
   * <li>Converts the LocalDate to start of day using the system default time zone</li>
   * <li>Converts to an Instant and extracts seconds since the Unix epoch</li>
   * <li>Converts the seconds to a string</li>
   * <li>Encodes the string as UTF-8 bytes</li>
   * </ol>
   *
   * @param source the LocalDate to convert
   * @return the byte array representation containing the timestamp as a UTF-8 string
   */
  @Override
  public byte[] convert(LocalDate source) {
    Instant instant = source.atStartOfDay(ZoneId.systemDefault()).toInstant();
    long unixTime = instant.getEpochSecond();
    return fromString(Long.toString(unixTime));
  }
}
