package com.redis.om.spring.serialization.gson;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.*;

/**
 * Gson type adapter for Date serialization and deserialization.
 */
public class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

  /**
   * Creates a new DateTypeAdapter.
   */
  public DateTypeAdapter() {
  }

  /**
   * Returns a singleton instance of DateTypeAdapter.
   *
   * @return the DateTypeAdapter instance
   */
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
