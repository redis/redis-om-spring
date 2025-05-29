package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

/**
 * Converter for reading byte arrays from Redis and converting them to Date objects.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to deserialize Date values when reading them from Redis. The conversion expects the
 * byte array to contain a UTF-8 encoded string representation of milliseconds since
 * the Unix epoch (January 1, 1970, 00:00:00 GMT).
 * </p>
 * <p>
 * The converter is automatically registered with Spring Data Redis when Redis OM Spring
 * is configured and is used transparently during entity deserialization operations.
 * </p>
 *
 * @see DateToBytesConverter
 * @see org.springframework.data.convert.ReadingConverter
 * @since 0.1.0
 */
@ReadingConverter
public class BytesToDateConverter implements Converter<byte[], Date> {

  /**
   * Default constructor for converter instantiation.
   */
  public BytesToDateConverter() {
  }

  /**
   * Converts a byte array containing milliseconds timestamp to a Date object.
   * <p>
   * The byte array is expected to contain a UTF-8 encoded string representation
   * of milliseconds since the Unix epoch.
   * </p>
   *
   * @param source the byte array containing the timestamp as a string
   * @return the corresponding Date object
   * @throws NumberFormatException if the byte array does not contain a valid long value
   */
  @Override
  public Date convert(byte[] source) {
    long milliseconds = Long.parseLong(toString(source));
    return new Date(milliseconds);
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