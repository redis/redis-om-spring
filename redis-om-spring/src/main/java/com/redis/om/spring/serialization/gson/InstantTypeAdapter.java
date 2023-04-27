package com.redis.om.spring.serialization.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;

public class InstantTypeAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

  @Override
  public JsonElement serialize(Instant instant, Type typeOfSrc, JsonSerializationContext context) {
    long timeInMillis = instant.getEpochSecond();
    return new JsonPrimitive(timeInMillis);
  }

  @Override
  public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return Instant.ofEpochSecond(json.getAsLong());
  }

  public static InstantTypeAdapter getInstance() {
    return new InstantTypeAdapter();
  }

}
