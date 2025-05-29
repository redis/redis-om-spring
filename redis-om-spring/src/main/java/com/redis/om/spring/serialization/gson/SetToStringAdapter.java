package com.redis.om.spring.serialization.gson;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * GSON TypeAdapter for serializing and deserializing Set collections to/from string format.
 * <p>
 * This adapter is part of Redis OM Spring's JSON serialization framework and provides
 * custom handling for Set collections when storing entity data in Redis. The adapter
 * converts Set collections to pipe-delimited strings for efficient storage and retrieval
 * in Redis JSON documents.
 * <p>
 * The serialization format uses the pipe character ('|') as a delimiter between set elements,
 * with each element converted to its string representation. During deserialization, the
 * pipe-delimited string is split back into individual elements to reconstruct the Set.
 * <p>
 * Serialization behavior:
 * <ul>
 * <li>Null or empty sets are serialized as JSON null values</li>
 * <li>Non-empty sets are converted to pipe-delimited strings</li>
 * <li>Each set element is converted using Object.toString()</li>
 * </ul>
 * <p>
 * Deserialization behavior:
 * <ul>
 * <li>JSON null values are deserialized as empty sets</li>
 * <li>String values are split on pipe characters to create set elements</li>
 * <li>All elements are treated as strings in the resulting set</li>
 * </ul>
 * <p>
 * Example transformations:
 * <pre>
 * Set.of("apple", "banana", "cherry") → "apple|banana|cherry"
 * Collections.emptySet() → null (JSON)
 * "red|green|blue" → Set.of("red", "green", "blue")
 * </pre>
 * <p>
 * This adapter is automatically registered with GSON when Redis OM Spring's
 * JSON serialization is configured, typically through RedisModulesConfiguration.
 * <p>
 * <strong>Note:</strong> This adapter assumes that set elements do not contain
 * the pipe character ('|') as this would interfere with the delimiter-based
 * serialization format.
 *
 * @see TypeAdapter
 * @see com.redis.om.spring.RedisModulesConfiguration
 * @see com.redis.om.spring.annotations.Document
 * 
 * @author Redis OM Spring Team
 * @since 1.0.0
 */
@SuppressWarnings(
  "rawtypes"
)
public class SetToStringAdapter extends TypeAdapter<Set<?>> {

  /**
   * Default constructor for the Set to String adapter.
   * <p>
   * This constructor is used by Gson's type adapter registration
   * mechanism to create adapter instances.
   */
  public SetToStringAdapter() {
    // Default constructor for Gson type adapter
  }

  /**
   * Serializes a Set to JSON as a pipe-delimited string.
   * <p>
   * Converts the provided Set to a JSON representation. Null or empty sets
   * are written as JSON null values, while non-empty sets are converted to
   * pipe-delimited strings with each element's string representation.
   *
   * @param writer the JsonWriter to write the serialized value to
   * @param value  the Set to serialize, may be null or empty
   * @throws IOException if an I/O error occurs during writing
   */
  @Override
  public void write(JsonWriter writer, Set<?> value) throws IOException {
    if (value == null || value.isEmpty()) {
      writer.nullValue();
      return;
    }
    writer.value(value.stream().map(Object::toString).collect(Collectors.joining("|")));
  }

  /**
   * Deserializes a JSON value to a Set by parsing a pipe-delimited string.
   * <p>
   * Reads a JSON value and converts it back to a Set collection. JSON null values
   * are converted to empty sets, while string values are split on pipe characters
   * to create the individual set elements.
   *
   * @param reader the JsonReader to read the serialized value from
   * @return a Set containing the deserialized elements, never null
   * @throws IOException if an I/O error occurs during reading or if the JSON
   *                     value is not null or a string
   */
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
