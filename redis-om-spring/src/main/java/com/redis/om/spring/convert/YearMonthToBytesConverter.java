package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * Converter for writing YearMonth values to Redis as byte arrays.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to serialize YearMonth values when storing them in Redis. The conversion strategy
 * formats the YearMonth as a string in "yyyy-MM" format and encodes that as a UTF-8
 * string in byte array format.
 * </p>
 * <p>
 * The converter is automatically registered with Spring Data Redis when Redis OM Spring
 * is configured and is used transparently during entity serialization operations.
 * </p>
 *
 * @see BytesToYearMonthConverter
 * @see org.springframework.data.convert.WritingConverter
 * @since 0.1.0
 */
@Component
@WritingConverter
public class YearMonthToBytesConverter implements Converter<YearMonth, byte[]> {
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

  /**
   * Default constructor for YearMonthToBytesConverter.
   * Initializes the converter with a DateTimeFormatter using the "yyyy-MM" pattern.
   */
  public YearMonthToBytesConverter() {
    // Default constructor - formatter is initialized inline
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
   * Converts a YearMonth value to a byte array representation.
   * <p>
   * The conversion formats the YearMonth using the "yyyy-MM" pattern
   * (e.g., "2023-12" for December 2023) and encodes it as UTF-8 bytes.
   * </p>
   *
   * @param source the YearMonth to convert
   * @return the byte array representation containing the formatted date as UTF-8 bytes
   */
  @Override
  public byte[] convert(YearMonth source) {
    return fromString(source.format(formatter));
  }
}
