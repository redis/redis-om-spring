package com.redis.om.spring.serialization.gson;

import java.lang.reflect.Type;
import java.time.Instant;

import com.google.gson.*;

/**
 * Gson type adapter for serializing and deserializing Instant objects.
 * This adapter converts Instant values to and from epoch milliseconds for JSON storage.
 * This is particularly useful for Redis JSON storage where Instant values need to be
 * represented as numeric timestamps.
 */
public class InstantTypeAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

  /**
   * Default constructor.
   * Creates a new instance of InstantTypeAdapter for serializing and deserializing Instant objects.
   */
  public InstantTypeAdapter() {
    // Default constructor
  }

  /**
   * Gets a singleton instance of the InstantTypeAdapter.
   *
   * @return a shared instance of InstantTypeAdapter
   */
  public static InstantTypeAdapter getInstance() {
    return new InstantTypeAdapter();
  }

  @Override
  public JsonElement serialize(Instant instant, Type typeOfSrc, JsonSerializationContext context) {
    long timeInMillis = instant.toEpochMilli();
    return new JsonPrimitive(timeInMillis);
  }

  @Override
  public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return Instant.ofEpochMilli(json.getAsLong());
  }

}
