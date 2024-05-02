package com.redis.om.spring.serialization.gson;

import com.google.gson.*;

import java.lang.reflect.Type;

public class EnumTypeAdapter<T extends Enum<?>> implements JsonSerializer<T>,
    JsonDeserializer<T> {

  private final T[] values;

  @SuppressWarnings("unchecked")
  public EnumTypeAdapter(Class<?> enumType) {
    this.values = (T[]) enumType.getEnumConstants();
  }

  public static <T extends Enum<?>> EnumTypeAdapter<T> of(Class<?> enumType) {
    return new EnumTypeAdapter<>(enumType);
  }

  @Override
  public JsonElement serialize(T o, Type type,
      JsonSerializationContext jsonSerializationContext) {
    return new JsonPrimitive(o.ordinal());
  }

  @Override
  public T deserialize(JsonElement json, Type type, JsonDeserializationContext context)
      throws JsonParseException {
    return values[json.getAsInt()];
  }
}
