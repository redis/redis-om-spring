package com.redis.om.spring.serialization.gson;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import com.google.gson.*;

/**
 * GSON Serializer/Deserializer for LocalDate to Unix Timestamp
 */
public class LocalDateTypeAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

  /**
   * Default constructor.
   */
  public LocalDateTypeAdapter() {
  }

  /**
   * Gets a singleton instance of the adapter.
   *
   * @return a LocalDateTypeAdapter instance
   */
  public static LocalDateTypeAdapter getInstance() {
    return new LocalDateTypeAdapter();
  }

  @Override
  public JsonElement serialize(LocalDate localDate, Type typeOfSrc, JsonSerializationContext context) {
    Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    long unixTime = instant.getEpochSecond();
    return new JsonPrimitive(unixTime);
  }

  @Override
  public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return LocalDate.ofInstant(Instant.ofEpochSecond(json.getAsLong()), ZoneId.systemDefault());
  }

}
