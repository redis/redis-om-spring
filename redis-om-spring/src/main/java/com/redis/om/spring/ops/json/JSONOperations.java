package com.redis.om.spring.ops.json;

import java.util.List;

import org.springframework.lang.Nullable;

import redis.clients.jedis.json.JsonSetParams;
import redis.clients.jedis.json.Path2;

/**
 * Interface defining operations for working with JSON documents stored in Redis.
 * This interface provides a high-level abstraction over RedisJSON commands,
 * allowing for type-safe operations on JSON documents with support for
 * JSONPath-based operations, bulk operations, and conditional updates.
 *
 * @param <K> the type of keys used to identify JSON documents
 */
public interface JSONOperations<K> {
  /**
   * Deletes a value at the specified JSONPath from a JSON document.
   *
   * @param key  the key of the JSON document
   * @param path the JSONPath pointing to the value to delete
   * @return the number of paths deleted
   */
  Long del(K key, Path2 path);

  /**
   * Gets the entire JSON document as a string.
   *
   * @param key the key of the JSON document
   * @return the JSON document as a string, or null if not found
   */
  @Nullable
  String get(K key);

  /**
   * Gets the entire JSON document deserialized to the specified type.
   *
   * @param <T>   the target type
   * @param key   the key of the JSON document
   * @param clazz the class to deserialize to
   * @return the deserialized object, or null if not found
   */
  @Nullable
  <T> T get(K key, Class<T> clazz);

  /**
   * Gets a value at the specified JSONPath from a JSON document.
   *
   * @param <T>   the target type
   * @param key   the key of the JSON document
   * @param clazz the class to deserialize to
   * @param path  the JSONPath pointing to the desired value
   * @return the deserialized object, or null if not found
   */
  @Nullable
  <T> T get(K key, Class<T> clazz, Path2 path);

  /**
   * Gets multiple JSON documents as strings in a single operation.
   *
   * @param keys the keys of the JSON documents to retrieve
   * @return a list of JSON documents as strings
   */
  @SuppressWarnings(
    "unchecked"
  )
  List<String> mget(K... keys);

  /**
   * Gets multiple JSON documents deserialized to the specified type.
   *
   * @param <T>   the target type
   * @param clazz the class to deserialize to
   * @param keys  the keys of the JSON documents to retrieve
   * @return a list of deserialized objects
   */
  @SuppressWarnings(
    "unchecked"
  )
  <T> List<T> mget(Class<T> clazz, K... keys);

  /**
   * Gets values at the specified JSONPath from multiple JSON documents.
   *
   * @param <T>   the target type
   * @param path  the JSONPath pointing to the desired values
   * @param clazz the class to deserialize to
   * @param keys  the keys of the JSON documents to retrieve from
   * @return a list of deserialized objects
   */
  @SuppressWarnings(
    "unchecked"
  )
  <T> List<T> mget(Path2 path, Class<T> clazz, K... keys);

  /**
   * Sets a JSON document at the root path.
   *
   * @param key    the key of the JSON document
   * @param object the object to serialize and store
   */
  void set(K key, Object object);

  /**
   * Sets a value at the specified JSONPath in a JSON document.
   *
   * @param key    the key of the JSON document
   * @param object the object to serialize and store
   * @param path   the JSONPath where to set the value
   */
  void set(K key, Object object, Path2 path);

  /**
   * Sets a JSON document at the root path with conditional parameters.
   *
   * @param key    the key of the JSON document
   * @param object the object to serialize and store
   * @param params the conditional parameters for the set operation
   */
  void set(K key, Object object, JsonSetParams params);

  /**
   * Sets a value at the specified JSONPath with conditional parameters.
   *
   * @param key    the key of the JSON document
   * @param object the object to serialize and store
   * @param params the conditional parameters for the set operation
   * @param path   the JSONPath where to set the value
   */
  void set(K key, Object object, JsonSetParams params, Path2 path);

  /**
   * Sets an escaped value at the specified JSONPath with conditional parameters.
   *
   * @param key    the key of the JSON document
   * @param object the object to serialize and store (will be escaped)
   * @param params the conditional parameters for the set operation
   * @param path   the JSONPath where to set the value
   */
  void setEscaped(K key, Object object, JsonSetParams params, Path2 path);

  /**
   * Gets the types of values in the JSON document at the root path.
   *
   * @param key the key of the JSON document
   * @return a list of classes representing the types at the root
   */
  List<Class<?>> type(K key);

  /**
   * Gets the types of values at the specified JSONPath.
   *
   * @param key  the key of the JSON document
   * @param path the JSONPath to check types for
   * @return a list of classes representing the types at the path
   */
  List<Class<?>> type(K key, Path2 path);

  /**
   * Appends a string to existing string values at the specified JSONPath.
   *
   * @param key    the key of the JSON document
   * @param path   the JSONPath pointing to string values
   * @param object the object to append (will be converted to string)
   * @return a list of the new lengths of the strings
   */
  List<Long> strAppend(K key, Path2 path, Object object);

  /**
   * Gets the length of string values at the specified JSONPath.
   *
   * @param key  the key of the JSON document
   * @param path the JSONPath pointing to string values
   * @return a list of string lengths
   */
  List<Long> strLen(K key, Path2 path);

  /**
   * Appends one or more values to array(s) at the specified JSONPath.
   *
   * @param key     the key of the JSON document
   * @param path    the JSONPath pointing to array values
   * @param objects the objects to append to the arrays
   * @return a list of the new lengths of the arrays
   */
  List<Long> arrAppend(K key, Path2 path, Object... objects);

  /**
   * Searches for the first occurrence of a scalar value in arrays at the specified JSONPath.
   *
   * @param key    the key of the JSON document
   * @param path   the JSONPath pointing to array values
   * @param scalar the value to search for
   * @return a list of indices where the value was found (-1 if not found)
   */
  List<Long> arrIndex(K key, Path2 path, Object scalar);

  /**
   * Inserts one or more values into arrays at the specified JSONPath and index.
   *
   * @param key     the key of the JSON document
   * @param path    the JSONPath pointing to array values
   * @param index   the index where to insert the values
   * @param objects the objects to insert into the arrays
   * @return a list of the new lengths of the arrays
   */
  List<Long> arrInsert(K key, Path2 path, Integer index, Object... objects);

  /**
   * Gets the length of arrays at the specified JSONPath.
   *
   * @param key  the key of the JSON document
   * @param path the JSONPath pointing to array values
   * @return a list of array lengths
   */
  List<Long> arrLen(K key, Path2 path);

  /**
   * Removes and returns an element from arrays at the specified JSONPath and index.
   *
   * @param <T>   the target type for the popped elements
   * @param key   the key of the JSON document
   * @param clazz the class to deserialize popped elements to
   * @param path  the JSONPath pointing to array values
   * @param index the index of the element to pop
   * @return a list of popped elements, or null if arrays are empty
   */
  @Nullable
  <T> List<T> arrPop(K key, Class<T> clazz, Path2 path, Integer index);

  /**
   * Removes and returns the last element from arrays at the specified JSONPath.
   *
   * @param <T>   the target type for the popped elements
   * @param key   the key of the JSON document
   * @param clazz the class to deserialize popped elements to
   * @param path  the JSONPath pointing to array values
   * @return a list of popped elements, or null if arrays are empty
   */
  @Nullable
  <T> List<T> arrPop(K key, Class<T> clazz, Path2 path);

  /**
   * Removes and returns the last element from the root array.
   *
   * @param <T>   the target type for the popped element
   * @param key   the key of the JSON document (must be an array)
   * @param clazz the class to deserialize the popped element to
   * @return a list containing the popped element, or null if array is empty
   */
  @Nullable
  <T> List<T> arrPop(K key, Class<T> clazz);

  /**
   * Trims arrays at the specified JSONPath to contain only elements within the given range.
   *
   * @param key   the key of the JSON document
   * @param path  the JSONPath pointing to array values
   * @param start the start index (inclusive)
   * @param stop  the stop index (inclusive)
   * @return a list of the new lengths of the trimmed arrays
   */
  List<Long> arrTrim(K key, Path2 path, Integer start, Integer stop);

  /**
   * Toggles boolean values at the specified JSONPath.
   *
   * @param key  the key of the JSON document
   * @param path the JSONPath pointing to boolean values
   */
  void toggle(K key, Path2 path);

  /**
   * Increments numeric values at the specified JSONPath by the given amount.
   *
   * @param key   the key of the JSON document
   * @param path  the JSONPath pointing to numeric values
   * @param value the amount to increment by
   * @return a list of the new values after incrementing
   */
  List<Double> numIncrBy(K key, Path2 path, Long value);
}
