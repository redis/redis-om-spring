package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

/**
 * Converter for reading byte arrays from Redis and converting them to YearMonth objects.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to deserialize YearMonth values when reading them from Redis. The conversion expects
 * the byte array to contain a UTF-8 encoded string in the format "yyyy-MM".
 * </p>
 * <p>
 * The converter is automatically registered with Spring Data Redis when Redis OM Spring
 * is configured and is used transparently during entity deserialization operations.
 * </p>
 *
 * @see YearMonthToBytesConverter
 * @see org.springframework.data.convert.ReadingConverter
 * @since 0.1.0
 */
@ReadingConverter
public class BytesToYearMonthConverter implements Converter<byte[], YearMonth> {
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

  /**
   * Creates a new BytesToYearMonthConverter.
   * <p>
   * This converter is typically instantiated automatically by the Redis OM Spring
   * conversion framework and does not require manual construction. The formatter
   * is initialized to handle the "yyyy-MM" format pattern.
   * </p>
   */
  public BytesToYearMonthConverter() {
    // Default constructor for Spring's automatic converter registration
  }

  /**
   * Converts a byte array containing a year-month string to a YearMonth object.
   * <p>
   * The byte array is expected to contain a UTF-8 encoded string in the format
   * "yyyy-MM" (e.g., "2023-12" for December 2023).
   * </p>
   *
   * @param source the byte array containing the year-month string
   * @return the corresponding YearMonth object
   * @throws java.time.format.DateTimeParseException if the string format is invalid
   */
  @Override
  public YearMonth convert(byte[] source) {
    return formatter.parse(toString(source), YearMonth::from);
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