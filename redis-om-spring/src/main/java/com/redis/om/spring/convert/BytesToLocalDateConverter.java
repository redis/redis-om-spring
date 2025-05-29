package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

/**
 * Converter for reading byte arrays from Redis and converting them to LocalDate objects.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to deserialize LocalDate values when reading them from Redis. The conversion expects
 * the byte array to contain a UTF-8 encoded string representation of seconds since
 * the Unix epoch, which is then converted to a LocalDate using the system default time zone.
 * </p>
 * <p>
 * The converter is automatically registered with Spring Data Redis when Redis OM Spring
 * is configured and is used transparently during entity deserialization operations.
 * </p>
 *
 * @see LocalDateToBytesConverter
 * @see org.springframework.data.convert.ReadingConverter
 * @since 0.1.0
 */
@ReadingConverter
public class BytesToLocalDateConverter implements Converter<byte[], LocalDate> {

  /**
   * Default constructor for converter instantiation.
   */
  public BytesToLocalDateConverter() {
  }

  /**
   * Converts a byte array containing epoch seconds to a LocalDate object.
   * <p>
   * The byte array is expected to contain a UTF-8 encoded string representation
   * of seconds since the Unix epoch. The conversion uses the system default time zone
   * to determine the local date.
   * </p>
   *
   * @param source the byte array containing the epoch seconds as a string
   * @return the corresponding LocalDate object
   * @throws NumberFormatException if the byte array does not contain a valid long value
   */
  @Override
  public LocalDate convert(byte[] source) {
    return LocalDate.ofInstant(Instant.ofEpochSecond(Long.parseLong(toString(source))), ZoneId.systemDefault());
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
