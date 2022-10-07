package com.redis.om.spring.serialization.gson;

import java.lang.reflect.Type;
import java.time.Instant;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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
