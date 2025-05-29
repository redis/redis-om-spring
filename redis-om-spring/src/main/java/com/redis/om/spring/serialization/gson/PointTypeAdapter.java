package com.redis.om.spring.serialization.gson;

import java.lang.reflect.Type;
import java.util.StringTokenizer;

import org.springframework.data.geo.Point;

import com.google.gson.*;

/**
 * Gson type adapter for serializing and deserializing Spring Data Point objects to/from JSON.
 * <p>
 * This adapter handles the conversion between Spring Data's geospatial Point objects and their
 * JSON representation for Redis OM Spring operations. The adapter supports both array format
 * [longitude, latitude] and string format "longitude,latitude" for deserialization, but always
 * serializes to the string format for consistency.
 * </p>
 * <p>
 * This type adapter is used internally by Redis OM Spring's JSON serialization framework to
 * ensure proper handling of geospatial data when storing and retrieving Point objects from Redis.
 * </p>
 *
 * @see org.springframework.data.geo.Point
 * @see com.google.gson.JsonSerializer
 * @see com.google.gson.JsonDeserializer
 * @since 0.1.0
 */
public class PointTypeAdapter implements JsonSerializer<Point>, JsonDeserializer<Point> {

  /**
   * Default constructor for the PointTypeAdapter.
   * <p>
   * Creates a new instance of the adapter that can be used to serialize and deserialize
   * Point objects to/from JSON format.
   * </p>
   */
  public PointTypeAdapter() {
    // Default constructor
  }

  /**
   * Returns a new instance of the PointTypeAdapter.
   * <p>
   * This is a convenience factory method that creates and returns a new adapter instance.
   * </p>
   *
   * @return a new PointTypeAdapter instance
   */
  public static PointTypeAdapter getInstance() {
    return new PointTypeAdapter();
  }

  /**
   * Serializes a Point object to JSON format.
   * <p>
   * The Point is serialized as a string in the format "longitude,latitude" where
   * longitude is the X coordinate and latitude is the Y coordinate.
   * </p>
   *
   * @param src       the Point object to serialize
   * @param typeOfSrc the type of the source object
   * @param context   the JSON serialization context
   * @return a JsonPrimitive containing the coordinates as "longitude,latitude"
   */
  @Override
  public JsonElement serialize(Point src, Type typeOfSrc, JsonSerializationContext context) {
    String lonlat = src.getX() + "," + src.getY();
    return new JsonPrimitive(lonlat);
  }

  /**
   * Deserializes a JSON element to a Point object.
   * <p>
   * This method supports two input formats:
   * <ul>
   * <li>JSON Array: [longitude, latitude] - array with two numeric elements</li>
   * <li>JSON String: "longitude,latitude" - comma-separated coordinate string</li>
   * </ul>
   * Both formats are parsed to create a Point with X as longitude and Y as latitude.
   *
   * @param json    the JSON element to deserialize
   * @param typeOfT the type of the target object
   * @param context the JSON deserialization context
   * @return a Point object with the parsed coordinates
   * @throws JsonParseException if the JSON format is invalid or coordinates cannot be parsed
   */
  @Override
  public Point deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    String lon;
    String lat;
    if (json.isJsonArray()) {
      JsonArray latlon = json.getAsJsonArray();
      lon = latlon.get(0).getAsString();
      lat = latlon.get(1).getAsString();
    } else {
      String latlon = json.getAsString();
      StringTokenizer st = new StringTokenizer(latlon, ",");
      lon = st.nextToken();
      lat = st.nextToken();
    }

    return new Point(Double.parseDouble(lon), Double.parseDouble(lat));
  }
}
