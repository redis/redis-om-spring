package com.redis.om.spring.serialization.gson;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LocalDateTimeTypeAdapter  implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime>{

  @Override
  public JsonElement serialize(LocalDateTime localDateTime, Type typeOfSrc, JsonSerializationContext context) {
    Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant(); 
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
