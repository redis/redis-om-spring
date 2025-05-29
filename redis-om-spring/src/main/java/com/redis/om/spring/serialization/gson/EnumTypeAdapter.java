package com.redis.om.spring.serialization.gson;

import java.lang.reflect.Type;

import com.google.gson.*;

/**
 * A Gson type adapter for serializing and deserializing enum values by their ordinal values.
 * This adapter serializes enums to their ordinal (integer) representation and deserializes
 * them back to the corresponding enum value.
 * 
 * @param <T> the enum type that extends Enum
 */
public class EnumTypeAdapter<T extends Enum<?>> implements JsonSerializer<T>, JsonDeserializer<T> {

  private final T[] values;

  /**
   * Constructs a new EnumTypeAdapter for the specified enum type.
   * 
   * @param enumType the enum class to create an adapter for
   */
  @SuppressWarnings(
    "unchecked"
  )
  public EnumTypeAdapter(Class<?> enumType) {
    this.values = (T[]) enumType.getEnumConstants();
  }

  /**
   * Factory method to create a new EnumTypeAdapter for the specified enum type.
   * 
   * @param <T>      the enum type that extends Enum
   * @param enumType the enum class to create an adapter for
   * @return a new EnumTypeAdapter instance
   */
  public static <T extends Enum<?>> EnumTypeAdapter<T> of(Class<?> enumType) {
    return new EnumTypeAdapter<>(enumType);
  }

  @Override
  public JsonElement serialize(T o, Type type, JsonSerializationContext jsonSerializationContext) {
    return new JsonPrimitive(o.ordinal());
  }

  @Override
  public T deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
    return values[json.getAsInt()];
  }
}
