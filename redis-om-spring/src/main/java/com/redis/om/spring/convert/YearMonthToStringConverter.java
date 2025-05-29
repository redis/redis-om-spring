package com.redis.om.spring.convert;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * Converter for writing YearMonth values to Redis as strings.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to serialize YearMonth values when storing them in Redis as string values. The conversion
 * strategy formats the YearMonth as a string in "yyyy-MM" format.
 * </p>
 * <p>
 * This converter is typically used for JSON document storage where YearMonth values need
 * to be represented as strings rather than binary data. The converter is automatically
 * registered with Spring Data Redis when Redis OM Spring is configured.
 * </p>
 *
 * @see YearMonthToBytesConverter
 * @see org.springframework.data.convert.WritingConverter
 * @since 0.1.0
 */
@Component
@WritingConverter
public class YearMonthToStringConverter implements Converter<YearMonth, String> {
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

  /**
   * Default constructor for YearMonthToStringConverter.
   * Initializes the converter with a DateTimeFormatter using the "yyyy-MM" pattern.
   */
  public YearMonthToStringConverter() {
    // Default constructor - formatter is initialized inline
  }

  /**
   * Converts a YearMonth value to its string representation.
   * <p>
   * The conversion formats the YearMonth using the "yyyy-MM" pattern
   * (e.g., "2023-12" for December 2023).
   * </p>
   *
   * @param source the YearMonth to convert
   * @return the string representation in "yyyy-MM" format
   */
  @Override
  public String convert(YearMonth source) {
    return source.format(formatter);
  }
}
