package com.redis.om.spring.ops.json;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.json.JSONArray;
import org.springframework.lang.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.redis.om.spring.client.RedisModulesClient;

import redis.clients.jedis.json.JsonSetParams;
import redis.clients.jedis.json.Path2;

/**
 * Implementation of JSON operations for Redis using RedisJSON module.
 * This class provides methods for storing, retrieving, and manipulating JSON documents
 * in Redis, with automatic serialization and deserialization using Gson.
 *
 * @param <K> the type of keys used to identify JSON documents
 */
public class JSONOperationsImpl<K> implements JSONOperations<K> {

  private final GsonBuilder builder;
  private final RedisModulesClient client;
  private Gson gson;

  /**
   * Constructs a new JSONOperationsImpl with the specified client and JSON builder.
   *
   * @param client  the Redis modules client for JSON operations
   * @param builder the Gson builder for JSON serialization/deserialization
   */
  public JSONOperationsImpl(RedisModulesClient client, GsonBuilder builder) {
    this.client = client;
    this.builder = builder;
  }

  /**
   * Deletes JSON data at the specified path for the given key.
   *
   * @param key  the key identifying the JSON document
   * @param path the JSON path to delete
   * @return the number of paths deleted
   */
  @Override
  public Long del(K key, Path2 path) {
    return client.clientForJSON().jsonDel(key.toString(), path);
  }

  /**
   * Retrieves the JSON document as a string for the given key.
   *
   * @param key the key identifying the JSON document
   * @return the JSON document as a string, or null if not found
   */
  @Nullable
  @Override
  public String get(K key) {
    var result = client.clientForJSON().jsonGet(key.toString(), Path2.ROOT_PATH);
    if (result instanceof JSONArray jsonArray) {
      return !jsonArray.isEmpty() ? jsonArray.get(0).toString() : null;
    } else if (result instanceof LinkedTreeMap<?, ?> linkedTreeMap) {
      return getGson().toJson(linkedTreeMap);
    } else {
      return result.toString();
    }
  }

  /**
   * Retrieves and deserializes the JSON document to the specified type.
   *
   * @param <T>   the type to deserialize to
   * @param key   the key identifying the JSON document
   * @param clazz the class to deserialize to
   * @return the deserialized object, or null if not found
   */
  @Override
  public <T> T get(K key, Class<T> clazz) {
    return get(key, clazz, Path2.ROOT_PATH);
  }

  /**
   * Retrieves and deserializes JSON data at the specified path to the specified type.
   *
   * @param <T>   the type to deserialize to
   * @param key   the key identifying the JSON document
   * @param clazz the class to deserialize to
   * @param path  the JSON path to retrieve
   * @return the deserialized object, or null if not found
   */
  @Override
  public <T> T get(K key, Class<T> clazz, Path2 path) {
    var result = client.clientForJSON().jsonGet(key.toString(), path);
    String asString;
    if (result instanceof JSONArray jsonArray) {
      return extractValueAsClassFromJSONArray(jsonArray, clazz);
    } else if (result instanceof LinkedTreeMap<?, ?> linkedTreeMap) {
      asString = getGson().toJson(linkedTreeMap);
    } else {
      asString = result != null ? result.toString() : null;
    }

    return result != null ? getGson().fromJson(asString, clazz) : null;
  }

  /**
   * Retrieves multiple JSON documents as strings for the given keys.
   *
   * @param keys the keys identifying the JSON documents
   * @return a list of JSON documents as strings
   */
  @SafeVarargs
  @Override
  public final List<String> mget(K... keys) {
    return (keys.length > 0) ?
        client.clientForJSON().jsonMGet(getKeysAsString(keys)).stream().filter(Objects::nonNull).map(jsonArr -> jsonArr
            .get(0)).map(Object::toString).toList() :
        List.of();
  }

  /**
   * Retrieves and deserializes multiple JSON documents to the specified type.
   *
   * @param <T>   the type to deserialize to
   * @param clazz the class to deserialize to
   * @param keys  the keys identifying the JSON documents
   * @return a list of deserialized objects
   */
  @SafeVarargs
  @Override
  public final <T> List<T> mget(Class<T> clazz, K... keys) {
    Gson g = getGson();
    return (keys.length > 0) ?
        client.clientForJSON().jsonMGet(getKeysAsString(keys)).stream().filter(Objects::nonNull).map(jsonArr -> jsonArr
            .get(0)).map(Object::toString).map(str -> g.fromJson(str, clazz)).toList() :
        List.of();
  }

  /**
   * Retrieves and deserializes JSON data at the specified path from multiple documents.
   *
   * @param <T>   the type to deserialize to
   * @param path  the JSON path to retrieve
   * @param clazz the class to deserialize to
   * @param keys  the keys identifying the JSON documents
   * @return a list of deserialized objects
   */
  @SafeVarargs
  @Override
  public final <T> List<T> mget(Path2 path, Class<T> clazz, K... keys) {
    Gson g = getGson();
    return (keys.length > 0) ?
        client.clientForJSON().jsonMGet(path, getKeysAsString(keys)).stream().map(Object::toString).map(str -> g
            .fromJson(str, clazz)).toList() :
        List.of();
  }

  /**
   * Sets a JSON document for the given key.
   *
   * @param key    the key to store the JSON document under
   * @param object the object to serialize and store as JSON
   */
  @Override
  public void set(K key, Object object) {
    client.clientForJSON().jsonSet(key.toString(), Path2.ROOT_PATH, getGson().toJson(object));
  }

  /**
   * Sets JSON data at the specified path for the given key.
   *
   * @param key    the key identifying the JSON document
   * @param object the object to serialize and store as JSON
   * @param path   the JSON path to set the data at
   */
  @Override
  public void set(K key, Object object, Path2 path) {
    client.clientForJSON().jsonSet(key.toString(), path, getGson().toJson(object));
  }

  /**
   * Sets a JSON document for the given key with specified parameters.
   *
   * @param key    the key to store the JSON document under
   * @param object the object to serialize and store as JSON
   * @param params the JSON set parameters (e.g., NX, XX conditions)
   */
  @Override
  public void set(K key, Object object, JsonSetParams params) {
    client.clientForJSON().jsonSet(key.toString(), object, params);
  }

  /**
   * Sets JSON data at the specified path with parameters for the given key.
   *
   * @param key    the key identifying the JSON document
   * @param object the object to serialize and store as JSON
   * @param params the JSON set parameters (e.g., NX, XX conditions)
   * @param path   the JSON path to set the data at
   */
  @Override
  public void set(K key, Object object, JsonSetParams params, Path2 path) {
    client.clientForJSON().jsonSet(key.toString(), path, object, params);
  }

  /**
   * Sets JSON data at the specified path with parameters and character escaping.
   *
   * @param key    the key identifying the JSON document
   * @param object the object to serialize and store as JSON
   * @param params the JSON set parameters (e.g., NX, XX conditions)
   * @param path   the JSON path to set the data at
   */
  @Override
  public void setEscaped(K key, Object object, JsonSetParams params, Path2 path) {
    client.clientForJSON().jsonSetWithEscape(key.toString(), path, object, params);
  }

  @Override
  public List<Class<?>> type(K key) {
    return type(key, Path2.ROOT_PATH);
  }

  @Override
  public List<Class<?>> type(K key, Path2 path) {
    return client.clientForJSON().jsonType(key.toString(), path);
  }

  @Override
  public List<Long> strAppend(K key, Path2 path, Object object) {
    return client.clientForJSON().jsonStrAppend(key.toString(), path, object);
  }

  @Override
  public List<Long> strLen(K key, Path2 path) {
    return client.clientForJSON().jsonStrLen(key.toString(), path);
  }

  @Override
  public List<Long> arrAppend(K key, Path2 path, Object... objects) {
    return client.clientForJSON().jsonArrAppendWithEscape(key.toString(), path, objects);
  }

  @Override
  public List<Long> arrIndex(K key, Path2 path, Object scalar) {
    return client.clientForJSON().jsonArrIndexWithEscape(key.toString(), path, scalar);
  }

  @Override
  public List<Long> arrInsert(K key, Path2 path, Integer index, Object... objects) {
    return client.clientForJSON().jsonArrInsertWithEscape(key.toString(), path, index, objects);
  }

  @Override
  public List<Long> arrLen(K key, Path2 path) {
    return client.clientForJSON().jsonArrLen(key.toString(), path);
  }

  @Override
  public <T> List<T> arrPop(K key, Class<T> clazz, Path2 path, Integer index) {
    return client.clientForJSON().jsonArrPop(key.toString(), path, index).stream().map(Object::toString).map(
        str -> getGson().fromJson(str, clazz)).toList();
  }

  @Override
  public <T> List<T> arrPop(K key, Class<T> clazz, Path2 path) {
    return client.clientForJSON().jsonArrPop(key.toString(), path).stream().map(Object::toString).map(str -> getGson()
        .fromJson(str, clazz)).toList();
  }

  @Override
  public <T> List<T> arrPop(K key, Class<T> clazz) {
    return arrPop(key, clazz, Path2.ROOT_PATH);
  }

  @Override
  public List<Long> arrTrim(K key, Path2 path, Integer start, Integer stop) {
    return client.clientForJSON().jsonArrTrim(key.toString(), path, start, stop);
  }

  @Override
  public void toggle(K key, Path2 path) {
    client.clientForJSON().jsonToggle(key.toString(), path);
  }

  @Override
  public List<Double> numIncrBy(K key, Path2 path, Long value) {
    JSONArray result = (JSONArray) client.clientForJSON().jsonNumIncrBy(key.toString(), path, value);
    return result.toList().stream().map(e -> Double.valueOf(e.toString())).toList();
  }

  /**
   * Converts an array of keys to an array of strings.
   *
   * @param keys the keys to convert
   * @return an array of string representations of the keys
   */
  @SafeVarargs
  private String[] getKeysAsString(K... keys) {
    return Arrays.stream(keys).map(Object::toString).toArray(String[]::new);
  }

  /**
   * Extracts and converts a value from a JSONArray to the specified class type.
   * Handles various primitive types and uses Gson for complex object deserialization.
   *
   * @param <T>       the type to convert to
   * @param jsonArray the JSONArray containing the value
   * @param clazz     the target class type
   * @return the converted value, or null if the array is empty
   */
  @SuppressWarnings(
    "unchecked"
  )
  private <T> T extractValueAsClassFromJSONArray(JSONArray jsonArray, Class<T> clazz) {
    if (jsonArray != null && !jsonArray.isEmpty()) {
      var element = jsonArray.get(0).toString();
      return switch (clazz.getSimpleName()) {
        case "String" -> element.equals("null") ? null : (T) element;
        case "Integer" -> (T) Integer.valueOf(element);
        case "Long" -> (T) Long.valueOf(element);
        case "Long[]" -> (T) jsonArray.toList().stream().map(e -> Long.valueOf(e.toString())).toArray(Long[]::new);
        case "Double" -> (T) Double.valueOf(element);
        case "Boolean" -> (T) Boolean.valueOf(element);
        default -> getGson().fromJson(element, clazz);
      };
    } else {
      return null;
    }
  }

  /**
   * Gets the Gson instance, creating it lazily if needed.
   *
   * @return the Gson instance for JSON serialization/deserialization
   */
  private Gson getGson() {
    if (gson == null) {
      gson = builder.create();
    }
    return gson;
  }
}