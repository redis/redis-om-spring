package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

/**
 * Converter for reading byte arrays from Redis and converting them to Boolean values.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to deserialize Boolean values when reading them from Redis. The conversion handles
 * multiple formats:
 * </p>
 * <ul>
 * <li>String "true" (case-insensitive) → {@code true}</li>
 * <li>String "1" → {@code true}</li>
 * <li>Any other value → {@code false}</li>
 * </ul>
 * <p>
 * The converter is automatically registered with Spring Data Redis when Redis OM Spring
 * is configured and is used transparently during entity deserialization operations.
 * </p>
 *
 * @see BooleanToBytesConverter
 * @see org.springframework.data.convert.ReadingConverter
 * @since 0.1.0
 */
@ReadingConverter
public class BytesToBooleanConverter implements Converter<byte[], Boolean> {

  /**
   * Default constructor for converter instantiation.
   */
  public BytesToBooleanConverter() {
  }

  /**
   * Converts a byte array to a Boolean value.
   * <p>
   * The conversion logic:
   * <ul>
   * <li>If the UTF-8 decoded string equals "1" or "true" (case-insensitive), returns {@code true}</li>
   * <li>Otherwise, returns {@code false}</li>
   * </ul>
   *
   * @param source the byte array to convert
   * @return the corresponding Boolean value
   */
  @Override
  public Boolean convert(byte[] source) {
    String value = toString(source);
    return ("1".equals(value) || "true".equalsIgnoreCase(value)) ? Boolean.TRUE : Boolean.FALSE;
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
