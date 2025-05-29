package com.redis.om.spring.util;

import static com.redis.om.spring.util.ObjectUtils.isPrimitiveOfType;

import java.util.Date;
import java.util.StringTokenizer;

import org.springframework.data.geo.Point;

import com.google.gson.Gson;

import redis.clients.jedis.util.SafeEncoder;

/**
 * Utility class for converting raw search result values to typed objects.
 * <p>
 * This converter handles the transformation of raw byte array values returned from
 * Redis Search operations into properly typed Java objects based on the target class.
 * It supports various data types including primitives, dates, geographical points,
 * and complex objects through JSON deserialization.
 * </p>
 */
public class SearchResultRawResponseToObjectConverter {

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private SearchResultRawResponseToObjectConverter() {
    // Utility class - no instantiation
  }

  /**
   * Processes a raw search result value and converts it to the specified target type.
   * <p>
   * This method handles various data type conversions:
   * </p>
   * <ul>
   * <li>Date objects from long timestamps</li>
   * <li>Point objects from comma-separated longitude,latitude strings</li>
   * <li>String values</li>
   * <li>Boolean values (from "1"/"0" strings)</li>
   * <li>Complex objects via JSON deserialization</li>
   * </ul>
   *
   * @param rawValue    the raw value from Redis search results (typically byte array)
   * @param targetClass the target class to convert the value to
   * @param gson        the Gson instance for JSON deserialization
   * @return the converted object of the specified target type, or null if rawValue is null
   */
  public static Object process(Object rawValue, Class<?> targetClass, Gson gson) {
    Object value = rawValue != null ? SafeEncoder.encode((byte[]) rawValue) : null;

    Object processValue = null;
    if (value != null) {
      if (targetClass == Date.class) {
        processValue = new Date(Long.parseLong(value.toString()));
      } else if (targetClass == Point.class) {
        StringTokenizer st = new StringTokenizer(value.toString(), ",");
        String lon = st.nextToken();
        String lat = st.nextToken();

        processValue = new Point(Double.parseDouble(lon), Double.parseDouble(lat));
      } else if (targetClass == String.class) {
        processValue = value.toString();
      } else if (targetClass == Boolean.class || isPrimitiveOfType(targetClass, Boolean.class)) {
        processValue = value.toString().equals("1");
      } else {
        processValue = gson.fromJson(value.toString(), targetClass);
      }
    }
    return processValue;
  }
}
