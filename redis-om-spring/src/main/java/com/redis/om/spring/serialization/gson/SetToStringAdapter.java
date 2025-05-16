package com.redis.om.spring.serialization.gson;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

@SuppressWarnings(
  "rawtypes"
)
public class SetToStringAdapter extends TypeAdapter<Set<?>> {

  @Override
  public void write(JsonWriter writer, Set<?> value) throws IOException {
    if (value == null || value.isEmpty()) {
      writer.nullValue();
      return;
    }
    writer.value(value.stream().map(Object::toString).collect(Collectors.joining("|")));
  }

  @Override
  public Set read(JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull();
      return Collections.emptySet();
    }
    String csv = reader.nextString();
    String[] parts = csv.split("\\|");
    return Set.of(parts);
  }

}
