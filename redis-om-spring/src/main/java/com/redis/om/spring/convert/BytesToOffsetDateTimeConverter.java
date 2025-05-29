package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

/**
 * Converter for reading byte arrays from Redis and converting them to OffsetDateTime objects.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to deserialize OffsetDateTime values when reading them from Redis. The conversion expects
 * the byte array to contain a UTF-8 encoded string representation of milliseconds since
 * the Unix epoch, which is then converted to an OffsetDateTime using the system default time zone.
 * </p>
 * <p>
 * The converter is automatically registered with Spring Data Redis when Redis OM Spring
 * is configured and is used transparently during entity deserialization operations.
 * </p>
 *
 * @see OffsetDateTimeToBytesConverter
 * @see org.springframework.data.convert.ReadingConverter
 * @since 0.1.0
 */
@ReadingConverter
public class BytesToOffsetDateTimeConverter implements Converter<byte[], OffsetDateTime> {

  /**
   * Default constructor for converter instantiation.
   */
  public BytesToOffsetDateTimeConverter() {
  }

  /**
   * Converts a byte array containing milliseconds timestamp to an OffsetDateTime object.
   * <p>
   * The byte array is expected to contain a UTF-8 encoded string representation
   * of milliseconds since the Unix epoch. The conversion uses the system default time zone
   * to determine the offset date and time.
   * </p>
   *
   * @param source the byte array containing the timestamp as a string
   * @return the corresponding OffsetDateTime object
   * @throws NumberFormatException if the byte array does not contain a valid long value
   */
  @Override
  public OffsetDateTime convert(byte[] source) {
    return OffsetDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(toString(source))), ZoneId.systemDefault());
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
