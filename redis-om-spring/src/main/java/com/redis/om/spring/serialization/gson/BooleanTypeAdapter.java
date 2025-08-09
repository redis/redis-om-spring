package com.redis.om.spring.serialization.gson;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Gson TypeAdapter for Boolean values that serializes them as 1/0 for RediSearch compatibility.
 * This adapter is specifically used for Boolean values in indexed Map fields to ensure
 * they are stored in a format that RediSearch TAG fields can query.
 */
public class BooleanTypeAdapter extends TypeAdapter<Boolean> {

  private static final BooleanTypeAdapter INSTANCE = new BooleanTypeAdapter();

  /**
   * Private constructor to enforce singleton pattern.
   * Use {@link #getInstance()} to obtain the singleton instance.
   */
  private BooleanTypeAdapter() {
    // Private constructor for singleton
  }

  /**
   * Returns the singleton instance of BooleanTypeAdapter.
   * This adapter serializes Boolean values as 1/0 for RediSearch compatibility.
   *
   * @return the singleton BooleanTypeAdapter instance
   */
  public static BooleanTypeAdapter getInstance() {
    return INSTANCE;
  }

  @Override
  public void write(JsonWriter out, Boolean value) throws IOException {
    if (value == null) {
      out.nullValue();
    } else {
      // Write Boolean as 1 or 0 for RediSearch TAG field compatibility
      out.value(value ? 1 : 0);
    }
  }

  @Override
  public Boolean read(JsonReader in) throws IOException {
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
      throw new IOException("Cannot parse boolean value: " + value);
    }
    throw new IOException("Expected BOOLEAN, NUMBER, or STRING but was " + token);
  }
}