package com.redis.om.spring.serialization.gson;

import java.lang.reflect.Type;

import com.github.f4b6a3.ulid.Ulid;
import com.google.gson.*;

/**
 * GSON type adapter for ULID (Universally Unique Lexicographically Sortable Identifier) serialization.
 * <p>
 * This adapter provides custom serialization and deserialization logic for ULID objects
 * when using GSON for JSON processing in Redis OM Spring. It handles the conversion
 * between ULID objects and their string representation in JSON documents.
 * </p>
 * <p>
 * ULIDs are serialized to their 26-character string representation using Crockford's
 * base32 encoding. This ensures that the lexicographic ordering properties of ULIDs
 * are preserved in the JSON representation, making them suitable for range queries
 * and sorting operations in Redis.
 * </p>
 * <p>
 * Example JSON representation:
 * <pre>{@code
 * {
 *   "id": "01ARZ3NDEKTSV4RRFFQ69G5FAV",
 *   "name": "John Doe"
 * }
 * }</pre>
 * <p>
 * This adapter is automatically registered with GSON when Redis OM Spring is configured
 * to use GSON for JSON serialization.
 * </p>
 *
 * @see com.github.f4b6a3.ulid.Ulid
 * @see JsonSerializer
 * @see JsonDeserializer
 * @since 0.1.0
 */
public class UlidTypeAdapter implements JsonSerializer<Ulid>, JsonDeserializer<Ulid> {

  /**
   * Creates a new instance of UlidTypeAdapter.
   * <p>
   * This constructor creates a new ULID type adapter for GSON serialization.
   * Consider using {@link #getInstance()} for obtaining instances of this adapter.
   * </p>
   */
  public UlidTypeAdapter() {
    // Default constructor
  }

  /**
   * Returns a singleton instance of the ULID type adapter.
   * <p>
   * This factory method provides a convenient way to obtain an instance of
   * the adapter for registration with GSON.
   * </p>
   *
   * @return a new instance of UlidTypeAdapter
   */
  public static UlidTypeAdapter getInstance() {
    return new UlidTypeAdapter();
  }

  /**
   * Serializes a ULID object to its JSON representation.
   * <p>
   * Converts the ULID to its 26-character string representation for storage
   * in JSON. The string format preserves the lexicographic ordering properties
   * of the ULID.
   * </p>
   *
   * @param src       the ULID object to serialize
   * @param typeOfSrc the actual type of the source object
   * @param context   the serialization context
   * @return a JsonPrimitive containing the ULID string representation
   */
  @Override
  public JsonElement serialize(Ulid src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.toString());
  }

  /**
   * Deserializes a JSON element to a ULID object.
   * <p>
   * Parses the 26-character string representation from JSON and converts it
   * back to a ULID object. The string must be a valid ULID format or a
   * JsonParseException will be thrown.
   * </p>
   *
   * @param json    the JSON element containing the ULID string
   * @param typeOfT the type of the desired object
   * @param context the deserialization context
   * @return the deserialized ULID object
   * @throws JsonParseException if the JSON element does not contain a valid ULID string
   */
  @Override
  public Ulid deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    String ulidAsString = json.getAsString();

    return Ulid.from(ulidAsString);
  }
}
