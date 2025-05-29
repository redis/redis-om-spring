package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.geo.Point;

/**
 * Converter for reading byte arrays from Redis and converting them to Point objects.
 * <p>
 * This converter is part of the Redis OM Spring data conversion framework and is used
 * to deserialize geospatial Point values when reading them from Redis. The conversion
 * expects the byte array to contain a UTF-8 encoded string in the format "longitude,latitude".
 * </p>
 * <p>
 * The converter is automatically registered with Spring Data Redis when Redis OM Spring
 * is configured and is used transparently during entity deserialization operations.
 * This is particularly useful for geospatial indexing and searching capabilities.
 * </p>
 *
 * @see PointToBytesConverter
 * @see org.springframework.data.convert.ReadingConverter
 * @see org.springframework.data.geo.Point
 * @since 0.1.0
 */
@ReadingConverter
public class BytesToPointConverter implements Converter<byte[], Point> {

  /**
   * Creates a new BytesToPointConverter.
   * <p>
   * This converter is typically instantiated automatically by the Redis OM Spring
   * conversion framework and does not require manual construction.
   * </p>
   */
  public BytesToPointConverter() {
    // Default constructor for Spring's automatic converter registration
  }

  /**
   * Converts a byte array containing longitude and latitude coordinates to a Point object.
   * <p>
   * The byte array is expected to contain a UTF-8 encoded string in the format
   * "longitude,latitude" (comma-separated values).
   * </p>
   *
   * @param source the byte array containing the coordinates as "longitude,latitude"
   * @return the corresponding Point object
   * @throws NumberFormatException            if the coordinates cannot be parsed as doubles
   * @throws java.util.NoSuchElementException if the format is invalid (missing comma or coordinate)
   */
  @Override
  public Point convert(byte[] source) {
    String latlon = toString(source);
    StringTokenizer st = new StringTokenizer(latlon, ",");
    String lon = st.nextToken();
    String lat = st.nextToken();

    return new Point(Double.parseDouble(lon), Double.parseDouble(lat));
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
