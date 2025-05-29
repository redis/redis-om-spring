package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * Converter for writing Date values to Redis as byte arrays.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to serialize Date values when storing them in Redis. The conversion strategy
 * converts the Date to milliseconds since the Unix epoch, then encodes that as a
 * UTF-8 string in byte array format.
 * </p>
 * <p>
 * The converter is automatically registered with Spring Data Redis when Redis OM Spring
 * is configured and is used transparently during entity serialization operations.
 * </p>
 *
 * @see BytesToDateConverter
 * @see org.springframework.data.convert.WritingConverter
 * @since 0.1.0
 */
@Component
@WritingConverter
public class DateToBytesConverter implements Converter<Date, byte[]> {

  /**
   * Creates a new DateToBytesConverter.
   */
  public DateToBytesConverter() {
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
   * Converts a Date value to a byte array representation.
   * <p>
   * The conversion extracts the milliseconds since the Unix epoch from the Date,
   * converts it to a string, and encodes it as UTF-8 bytes.
   * </p>
   *
   * @param source the Date to convert
   * @return the byte array representation containing the timestamp as a UTF-8 string
   */
  @Override
  public byte[] convert(Date source) {
    return fromString(Long.toString(source.getTime()));
  }
}