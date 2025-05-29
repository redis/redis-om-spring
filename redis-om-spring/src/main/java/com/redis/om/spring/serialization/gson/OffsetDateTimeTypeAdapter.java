package com.redis.om.spring.serialization.gson;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import com.google.gson.*;

/**
 * Gson type adapter for serializing and deserializing OffsetDateTime values to and from JSON.
 * <p>
 * This adapter converts OffsetDateTime values to milliseconds since the Unix epoch for JSON
 * serialization and converts JSON numeric values back to OffsetDateTime instances for
 * deserialization. The adapter uses the system default time zone when reconstructing
 * OffsetDateTime instances from epoch milliseconds.
 * </p>
 * <p>
 * This type adapter is typically registered with Gson to handle OffsetDateTime fields
 * in Redis OM Spring entities when using JSON serialization.
 * </p>
 *
 * @see com.google.gson.JsonSerializer
 * @see com.google.gson.JsonDeserializer
 * @see java.time.OffsetDateTime
 * @since 0.1.0
 */
public class OffsetDateTimeTypeAdapter implements JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {

  /**
   * Default constructor for OffsetDateTimeTypeAdapter.
   * <p>
   * Creates a new instance of the type adapter. This constructor is used
   * when creating instances via the static factory method or direct instantiation.
   * </p>
   */
  public OffsetDateTimeTypeAdapter() {
    // Default constructor for type adapter instantiation
  }

  /**
   * Factory method to create a new instance of OffsetDateTimeTypeAdapter.
   * <p>
   * This method provides a convenient way to obtain an instance of the type adapter
   * for registration with Gson configurations.
   * </p>
   *
   * @return a new instance of OffsetDateTimeTypeAdapter
   */
  public static OffsetDateTimeTypeAdapter getInstance() {
    return new OffsetDateTimeTypeAdapter();
  }

  /**
   * Serializes an OffsetDateTime to a JSON element containing milliseconds since Unix epoch.
   * <p>
   * The serialization converts the OffsetDateTime to an Instant and then to epoch milliseconds,
   * which is stored as a JSON primitive numeric value.
   * </p>
   *
   * @param offsetDateTime the OffsetDateTime to serialize
   * @param typeOfSrc      the type of the source object
   * @param context        the JSON serialization context
   * @return a JsonPrimitive containing the epoch milliseconds
   */
  public JsonElement serialize(OffsetDateTime offsetDateTime, Type typeOfSrc, JsonSerializationContext context) {
    long timeInMillis = offsetDateTime.toInstant().toEpochMilli();
    return new JsonPrimitive(timeInMillis);
  }

  /**
   * Deserializes a JSON element containing epoch milliseconds back to an OffsetDateTime.
   * <p>
   * The deserialization converts the JSON numeric value to an Instant using epoch milliseconds,
   * then creates an OffsetDateTime using the system default time zone.
   * </p>
   *
   * @param json    the JSON element containing the epoch milliseconds
   * @param typeOfT the type of the target object
   * @param context the JSON deserialization context
   * @return an OffsetDateTime instance created from the epoch milliseconds
   * @throws JsonParseException if the JSON element cannot be parsed as a long value
   */
  public OffsetDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return OffsetDateTime.ofInstant(Instant.ofEpochMilli(json.getAsLong()), ZoneId.systemDefault());
  }
}
