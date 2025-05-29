package com.redis.om.spring.serialization.gson;

import java.lang.reflect.Type;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import com.google.gson.*;

/**
 * Gson type adapter for serializing and deserializing {@link YearMonth} objects.
 * <p>
 * This adapter handles the conversion between YearMonth objects and their JSON representation
 * using the "yyyy-MM" format (e.g., "2023-12" for December 2023). It implements both
 * {@link JsonSerializer} and {@link JsonDeserializer} interfaces to provide bidirectional
 * conversion support.
 * </p>
 * <p>
 * This adapter is used internally by Redis OM Spring when Gson is used for JSON serialization
 * of entities containing YearMonth fields.
 * </p>
 *
 * @see YearMonth
 * @see JsonSerializer
 * @see JsonDeserializer
 */
public class YearMonthTypeAdapter implements JsonSerializer<YearMonth>, JsonDeserializer<YearMonth> {

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

  /**
   * Default constructor for YearMonthTypeAdapter.
   * Initializes the adapter with a DateTimeFormatter using the "yyyy-MM" pattern.
   */
  public YearMonthTypeAdapter() {
    // Default constructor - formatter is initialized inline
  }

  /**
   * Creates a new instance of YearMonthTypeAdapter.
   * <p>
   * This factory method provides a convenient way to create instances of the adapter
   * for registration with Gson.
   * </p>
   *
   * @return a new instance of YearMonthTypeAdapter
   */
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
