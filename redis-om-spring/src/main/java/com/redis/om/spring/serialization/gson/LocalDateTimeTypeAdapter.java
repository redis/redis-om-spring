package com.redis.om.spring.serialization.gson;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.google.gson.*;

/**
 * Gson type adapter for serializing and deserializing LocalDateTime objects.
 * This adapter converts LocalDateTime values to and from epoch milliseconds for JSON storage.
 * This is particularly useful for Redis JSON storage where LocalDateTime values need to be
 * represented as numeric timestamps for range searches and indexing.
 * <p>
 * NOTE: This Adapter will lose nanosecond precision on LocalDateTimes.
 * In order to perform range searches we need to store this as Gson serialized Java longs
 * so that they can be indexed as NUMERIC in the index's schema.
 * </p>
 */
public class LocalDateTimeTypeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

  /**
   * Default constructor.
   * Creates a new instance of LocalDateTimeTypeAdapter for serializing and deserializing LocalDateTime objects.
   */
  public LocalDateTimeTypeAdapter() {
    // Default constructor
  }

  /**
   * Gets a singleton instance of the LocalDateTimeTypeAdapter.
   *
   * @return a shared instance of LocalDateTimeTypeAdapter
   */
  public static LocalDateTimeTypeAdapter getInstance() {
    return new LocalDateTimeTypeAdapter();
  }

  /**
   * Serializes a LocalDateTime to JSON as epoch milliseconds.
   *
   * @param localDateTime the LocalDateTime to serialize
   * @param typeOfSrc     the type of the source object
   * @param context       the serialization context
   * @return JsonElement containing the epoch milliseconds
   */
  @Override
  public JsonElement serialize(LocalDateTime localDateTime, Type typeOfSrc, JsonSerializationContext context) {
    Instant instant = ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant();
    long timeInMillis = instant.toEpochMilli();
    return new JsonPrimitive(timeInMillis);
  }

  /**
   * Deserializes JSON epoch milliseconds to a LocalDateTime.
   *
   * @param json    the JSON element containing epoch milliseconds
   * @param typeOfT the type of the target object
   * @param context the deserialization context
   * @return the deserialized LocalDateTime
   * @throws JsonParseException if the JSON cannot be parsed
   */
  @Override
  public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(json.getAsLong()), ZoneId.systemDefault());
  }

}
