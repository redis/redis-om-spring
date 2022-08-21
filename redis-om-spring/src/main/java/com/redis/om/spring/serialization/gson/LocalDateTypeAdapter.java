package com.redis.om.spring.serialization.gson;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * GSON Serializer/Deserializer for LocalDate to Unix Timestamp
 *
 */
public class LocalDateTypeAdapter  implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate>{

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
  
  public static LocalDateTypeAdapter getInstance() {
    return new LocalDateTypeAdapter();
  }

}
