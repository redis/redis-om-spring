package com.redis.om.spring.serialization.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class OffsetDateTimeTypeAdapter implements JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {

  public static OffsetDateTimeTypeAdapter getInstance() {
    return new OffsetDateTimeTypeAdapter();
  }

  public JsonElement serialize(OffsetDateTime offsetDateTime, Type typeOfSrc, JsonSerializationContext context) {
    long timeInMillis = offsetDateTime.toInstant().toEpochMilli();
    return new JsonPrimitive(timeInMillis);
  }

  public OffsetDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return OffsetDateTime.ofInstant(Instant.ofEpochMilli(json.getAsLong()), ZoneId.systemDefault());
  }
}
