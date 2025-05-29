package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.github.f4b6a3.ulid.Ulid;

/**
 * Converter for reading byte arrays from Redis and converting them to ULID objects.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to deserialize ULID (Universally Unique Lexicographically Sortable Identifier) values
 * when reading them from Redis. The conversion expects the byte array to contain a UTF-8
 * encoded string representation of a valid ULID.
 * </p>
 * <p>
 * ULIDs are Redis OM Spring's default identifier format, providing better performance
 * than UUIDs due to their lexicographic ordering properties. The converter is automatically
 * registered with Spring Data Redis when Redis OM Spring is configured.
 * </p>
 *
 * @see UlidToBytesConverter
 * @see org.springframework.data.convert.ReadingConverter
 * @see com.github.f4b6a3.ulid.Ulid
 * @since 0.1.0
 */
@ReadingConverter
public class BytesToUlidConverter implements Converter<byte[], Ulid> {

  /**
   * Creates a new BytesToUlidConverter.
   * <p>
   * This converter is typically instantiated automatically by the Redis OM Spring
   * conversion framework and does not require manual construction.
   * </p>
   */
  public BytesToUlidConverter() {
    // Default constructor for Spring's automatic converter registration
  }

  /**
   * Converts a byte array containing a ULID string to a Ulid object.
   * <p>
   * The byte array is expected to contain a UTF-8 encoded string representation
   * of a valid ULID (26 characters, base32 encoded).
   * </p>
   *
   * @param source the byte array containing the ULID string
   * @return the corresponding Ulid object
   * @throws IllegalArgumentException if the string is not a valid ULID format
   */
  @Override
  public Ulid convert(byte[] source) {
    return Ulid.from(toString(source));
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
