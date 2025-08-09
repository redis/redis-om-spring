package com.redis.om.spring.serialization.gson;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * TypeAdapterFactory that handles Boolean values in Maps specially for RediSearch compatibility.
 * When serializing Map values with Boolean type, Booleans are converted to 1/0 instead of true/false
 * to ensure compatibility with RediSearch TAG fields.
 */
public class MapBooleanTypeAdapterFactory implements TypeAdapterFactory {

  private static final MapBooleanTypeAdapterFactory INSTANCE = new MapBooleanTypeAdapterFactory();

  /**
   * Private constructor to enforce singleton pattern.
   * Use {@link #getInstance()} to obtain the singleton instance.
   */
  private MapBooleanTypeAdapterFactory() {
    // Private constructor for singleton
  }

  /**
   * Returns the singleton instance of MapBooleanTypeAdapterFactory.
   * This factory creates TypeAdapters for Map types with Boolean values,
   * serializing them as 1/0 for RediSearch compatibility.
   *
   * @return the singleton MapBooleanTypeAdapterFactory instance
   */
  public static MapBooleanTypeAdapterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  @SuppressWarnings(
    "unchecked"
  )
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    // Only handle Map types
    if (!Map.class.isAssignableFrom(type.getRawType())) {
      return null;
    }

    Type mapType = type.getType();
    if (!(mapType instanceof ParameterizedType)) {
      return null;
    }

    ParameterizedType parameterizedType = (ParameterizedType) mapType;
    Type[] typeArguments = parameterizedType.getActualTypeArguments();

    // Check if this is a Map with Boolean values
    if (typeArguments.length != 2) {
      return null;
    }

    Type valueType = typeArguments[1];
    boolean isBooleanValue = (valueType == Boolean.class || valueType == boolean.class);

    if (!isBooleanValue) {
      // Not a Boolean map, delegate to default adapter
      return null;
    }

    // Get the default Map adapter
    TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

    // Return our custom adapter that wraps the delegate
    return new TypeAdapter<T>() {
      @Override
      public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
          out.nullValue();
          return;
        }

        Map<?, Boolean> map = (Map<?, Boolean>) value;
        out.beginObject();

        for (Map.Entry<?, Boolean> entry : map.entrySet()) {
          if (entry.getKey() != null) {
            out.name(String.valueOf(entry.getKey()));
            if (entry.getValue() == null) {
              out.nullValue();
            } else {
              // Write Boolean as 1 or 0 for RediSearch TAG field compatibility
              out.value(entry.getValue() ? 1 : 0);
            }
          }
        }

        out.endObject();
      }

      @Override
      public T read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
          in.nextNull();
          return null;
        }

        Map<String, Boolean> map = new HashMap<>();
        in.beginObject();

        while (in.hasNext()) {
          String key = in.nextName();
          Boolean value = readBooleanValue(in);
          map.put(key, value);
        }

        in.endObject();
        return (T) map;
      }

      private Boolean readBooleanValue(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
          in.nextNull();
          return null;
        } else if (token == JsonToken.BOOLEAN) {
          return in.nextBoolean();
        } else if (token == JsonToken.NUMBER) {
          // Read 1 as true, 0 as false
          int value = in.nextInt();
          return value != 0;
        } else if (token == JsonToken.STRING) {
          String value = in.nextString();
          // Handle string representations
          if ("1".equals(value) || "true".equalsIgnoreCase(value)) {
            return true;
          } else if ("0".equals(value) || "false".equalsIgnoreCase(value)) {
            return false;
          }
          throw new JsonParseException("Cannot parse boolean value: " + value);
        }
        throw new JsonParseException("Expected BOOLEAN, NUMBER, or STRING but was " + token);
      }
    };
  }
}