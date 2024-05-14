package com.redis.om.spring.serialization.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Date;

public class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

  public static DateTypeAdapter getInstance() {
    return new DateTypeAdapter();
  }

  @Override
  public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(date.getTime());
  }

  @Override
  public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
    throws JsonParseException {
    return new Date(json.getAsLong());
  }

}
