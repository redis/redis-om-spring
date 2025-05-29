package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

/**
 * Converter for reading byte arrays from Redis and converting them to LocalDateTime objects.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to deserialize LocalDateTime values when reading them from Redis. The conversion expects
 * the byte array to contain a UTF-8 encoded string representation of milliseconds since
 * the Unix epoch, which is then converted to a LocalDateTime using the system default time zone.
 * </p>
 * <p>
 * The converter is automatically registered with Spring Data Redis when Redis OM Spring
 * is configured and is used transparently during entity deserialization operations.
 * </p>
 *
 * @see LocalDateTimeToBytesConverter
 * @see org.springframework.data.convert.ReadingConverter
 * @since 0.1.0
 */
@ReadingConverter
public class BytesToLocalDateTimeConverter implements Converter<byte[], LocalDateTime> {

  /**
   * Default constructor for converter instantiation.
   */
  public BytesToLocalDateTimeConverter() {
  }

  /**
   * Converts a byte array containing milliseconds timestamp to a LocalDateTime object.
   * <p>
   * The byte array is expected to contain a UTF-8 encoded string representation
   * of milliseconds since the Unix epoch. The conversion uses the system default time zone
   * to determine the local date and time.
   * </p>
   *
   * @param source the byte array containing the timestamp as a string
   * @return the corresponding LocalDateTime object
   * @throws NumberFormatException if the byte array does not contain a valid long value
   */
  @Override
  public LocalDateTime convert(byte[] source) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(toString(source))), ZoneId.systemDefault());
  }

  /**
   * Converts a byte array to its UTF-8 string representation.
   *
   * @param source the byte array to convert
   * @return the UTF-8 decoded string
   */
  String toString(byte[] source) {
    return new String(source, StandardCharsets.UTF_8);
  }

}
