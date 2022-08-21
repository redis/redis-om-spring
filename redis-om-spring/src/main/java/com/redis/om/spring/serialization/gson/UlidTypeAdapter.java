package com.redis.om.spring.serialization.gson;

import java.lang.reflect.Type;

import com.github.f4b6a3.ulid.Ulid;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class UlidTypeAdapter implements JsonSerializer<Ulid>, JsonDeserializer<Ulid>{

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
  
  public static UlidTypeAdapter getInstance() {
    return new UlidTypeAdapter();
  }
}
