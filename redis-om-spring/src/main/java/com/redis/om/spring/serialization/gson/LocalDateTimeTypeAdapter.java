package com.redis.om.spring.serialization.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * NOTE: This Adapter will lose nanosecond precision on LocalDateTimes
 * In order to perform range searches we need to store this as GSon serialized Java longs
 * so that they can be indexed as NUMERIC in the index's schema
 */
public class LocalDateTimeTypeAdapter  implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime>{

  @Override
  public JsonElement serialize(LocalDateTime localDateTime, Type typeOfSrc, JsonSerializationContext context) {
    Instant instant = ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant();
    long timeInMillis = instant.toEpochMilli(); 
    return new JsonPrimitive(timeInMillis);
  }
  
  @Override
  public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(json.getAsLong()), ZoneId.systemDefault());
  }
  
  public static LocalDateTimeTypeAdapter getInstance() {
    return new LocalDateTimeTypeAdapter();
  }

}
