package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import com.github.f4b6a3.ulid.Ulid;

/**
 * Converter for writing ULID values to Redis as byte arrays.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to serialize ULID (Universally Unique Lexicographically Sortable Identifier) values
 * when storing them in Redis. The conversion strategy converts the ULID to its string
 * representation and encodes that as a UTF-8 string in byte array format.
 * </p>
 * <p>
 * ULIDs are Redis OM Spring's default identifier format, providing better performance
 * than UUIDs due to their lexicographic ordering properties. The converter is automatically
 * registered with Spring Data Redis when Redis OM Spring is configured.
 * </p>
 *
 * @see BytesToUlidConverter
 * @see org.springframework.data.convert.WritingConverter
 * @see com.github.f4b6a3.ulid.Ulid
 * @since 0.1.0
 */
@Component
@WritingConverter
public class UlidToBytesConverter implements Converter<Ulid, byte[]> {

  /**
   * Creates a new instance of UlidToBytesConverter.
   * <p>
   * This converter is automatically registered with Spring Data Redis
   * when Redis OM Spring is configured.
   * </p>
   */
  public UlidToBytesConverter() {
    // Default constructor
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
   * Converts a ULID value to a byte array representation.
   * <p>
   * The conversion uses the ULID's string representation (26 characters, base32 encoded)
   * and encodes it as UTF-8 bytes.
   * </p>
   *
   * @param source the ULID to convert
   * @return the byte array representation containing the ULID string as UTF-8 bytes
   */
  @Override
  public byte[] convert(Ulid source) {
    return fromString(source.toString());
  }
}
