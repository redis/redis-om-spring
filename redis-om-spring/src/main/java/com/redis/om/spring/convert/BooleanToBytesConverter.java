
package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * Converter for writing Boolean values to Redis as byte arrays.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to serialize Boolean values when storing them in Redis. The conversion strategy:
 * </p>
 * <ul>
 * <li>{@code true} → "true" as UTF-8 bytes</li>
 * <li>{@code false} → "false" as UTF-8 bytes</li>
 * </ul>
 * <p>
 * The converter is automatically registered with Spring Data Redis when Redis OM Spring
 * is configured and is used transparently during entity serialization operations.
 * </p>
 *
 * @see BytesToBooleanConverter
 * @see org.springframework.data.convert.WritingConverter
 * @since 1.0
 */
@Component
@WritingConverter
public class BooleanToBytesConverter implements Converter<Boolean, byte[]> {
  private final byte[] trueAsBytes = fromString("true");
  private final byte[] falseAsBytes = fromString("false");

  /**
   * Default constructor for Spring component instantiation.
   */
  public BooleanToBytesConverter() {
  }

  byte[] fromString(String source) {
    return source.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public byte[] convert(Boolean source) {
    return Boolean.TRUE.equals(source) ? trueAsBytes : falseAsBytes;
  }
}
