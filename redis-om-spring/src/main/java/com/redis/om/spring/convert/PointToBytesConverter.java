package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;

/**
 * Converter for writing Point values to Redis as byte arrays.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to serialize geospatial Point values when storing them in Redis. The conversion strategy
 * formats the Point coordinates as "longitude,latitude" and encodes that as a UTF-8 string
 * in byte array format.
 * </p>
 * <p>
 * The converter is automatically registered with Spring Data Redis when Redis OM Spring
 * is configured and is used transparently during entity serialization operations.
 * This is particularly useful for geospatial indexing and searching capabilities.
 * </p>
 *
 * @see BytesToPointConverter
 * @see org.springframework.data.convert.WritingConverter
 * @see org.springframework.data.geo.Point
 * @since 0.1.0
 */
@Component
@WritingConverter
public class PointToBytesConverter implements Converter<Point, byte[]> {

  /**
   * Default constructor for PointToBytesConverter.
   */
  public PointToBytesConverter() {
    // Default constructor for converter instantiation
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
   * Converts a Point value to a byte array representation.
   * <p>
   * The conversion formats the Point coordinates as "longitude,latitude" (comma-separated)
   * and encodes that string as UTF-8 bytes.
   * </p>
   *
   * @param source the Point to convert (where X is longitude and Y is latitude)
   * @return the byte array representation containing the coordinates as "X,Y"
   */
  @Override
  public byte[] convert(Point source) {
    return fromString(source.getX() + "," + source.getY());
  }
}
