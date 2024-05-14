package com.redis.om.spring.serialization.gson;

import com.github.f4b6a3.ulid.Ulid;
import com.google.gson.*;

import java.lang.reflect.Type;

public class UlidTypeAdapter implements JsonSerializer<Ulid>, JsonDeserializer<Ulid> {

  public static UlidTypeAdapter getInstance() {
    return new UlidTypeAdapter();
  }

  @Override
  public JsonElement serialize(Ulid src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.toString());
  }

  @Override
  public Ulid deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
    throws JsonParseException {
    String ulidAsString = json.getAsString();

    return Ulid.from(ulidAsString);
  }
}
