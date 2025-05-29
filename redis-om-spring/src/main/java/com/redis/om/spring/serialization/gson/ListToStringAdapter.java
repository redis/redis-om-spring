package com.redis.om.spring.serialization.gson;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Gson type adapter for serializing and deserializing List objects to/from pipe-delimited strings.
 * This adapter converts List values to pipe-separated strings and back, making them suitable
 * for storage in Redis where complex objects need string representation.
 */
@SuppressWarnings(
  "rawtypes"
)
public class ListToStringAdapter extends TypeAdapter<List<?>> {

  /**
   * Default constructor.
   * Creates a new instance of ListToStringAdapter for serializing and deserializing List objects.
   */
  public ListToStringAdapter() {
    // Default constructor
  }

  /**
   * Writes a List to JSON as a pipe-delimited string.
   *
   * @param writer the JsonWriter to write to
   * @param value  the List to serialize
   * @throws IOException if an I/O error occurs
   */
  @Override
  public void write(JsonWriter writer, List<?> value) throws IOException {
    if (value == null || value.isEmpty()) {
      writer.nullValue();
      return;
    }
    writer.value(value.stream().map(Object::toString).collect(Collectors.joining("|")));
  }

  /**
   * Reads a pipe-delimited string from JSON and converts it to a List.
   *
   * @param reader the JsonReader to read from
   * @return the deserialized List
   * @throws IOException if an I/O error occurs
   */
  @Override
  public List read(JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull();
      return Collections.emptyList();
    }
    String csv = reader.nextString();
    String[] parts = csv.split("\\|");
    return List.of(parts);
  }

}
