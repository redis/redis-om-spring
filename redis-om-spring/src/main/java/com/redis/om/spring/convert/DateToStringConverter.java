package com.redis.om.spring.convert;

import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * Converter for writing Date values to Redis as strings.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to serialize Date values when storing them in Redis as string values. The conversion
 * strategy converts the Date to milliseconds since the Unix epoch and formats it as
 * a string representation.
 * </p>
 * <p>
 * This converter is typically used for JSON document storage where Date values need
 * to be represented as strings rather than binary data. The converter is automatically
 * registered with Spring Data Redis when Redis OM Spring is configured.
 * </p>
 *
 * @see DateToBytesConverter
 * @see org.springframework.data.convert.WritingConverter
 * @since 0.1.0
 */
@Component
@WritingConverter
public class DateToStringConverter implements Converter<Date, String> {

  /**
   * Creates a new DateToStringConverter.
   */
  public DateToStringConverter() {
  }

  /**
   * Converts a Date value to its string representation.
   * <p>
   * The conversion extracts the milliseconds since the Unix epoch from the Date
   * and converts it to a string.
   * </p>
   *
   * @param source the Date to convert
   * @return the string representation of the timestamp in milliseconds
   */
  @Override
  public String convert(Date source) {
    return Long.toString(source.getTime());
  }
}