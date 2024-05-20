package com.redis.om.spring.serialization.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class YearMonthTypeAdapter implements JsonSerializer<YearMonth>, JsonDeserializer<YearMonth> {

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

  public static YearMonthTypeAdapter getInstance() {
    return new YearMonthTypeAdapter();
  }

  @Override
  public JsonElement serialize(YearMonth yearMonth, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(yearMonth.format(formatter));
  }

  @Override
  public YearMonth deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
    throws JsonParseException {
    return YearMonth.parse(json.getAsString(), formatter);
  }

}
