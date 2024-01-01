package com.redis.om.spring.ops.json;

import org.springframework.lang.Nullable;
import redis.clients.jedis.json.JsonSetParams;
import redis.clients.jedis.json.Path2;

import java.util.List;

public interface JSONOperations<K> {
  Long del(K key, Path2 path);

  @Nullable
  String get(K key);

  @Nullable
  <T> T get(K key, Class<T> clazz);

  @Nullable
  <T> T get(K key, Class<T> clazz, Path2 path);

  @SuppressWarnings("unchecked")
  List<String> mget(K... keys);

  @SuppressWarnings("unchecked")
  <T> List<T> mget(Class<T> clazz, K... keys);

  @SuppressWarnings("unchecked")
  <T> List<T> mget(Path2 path, Class<T> clazz, K... keys);

  void set(K key, Object object);

  void set(K key, Object object, Path2 path);

  void set(K key, Object object, JsonSetParams params);

  void set(K key, Object object, JsonSetParams params, Path2 path);

  void setEscaped(K key, Object object, JsonSetParams params, Path2 path);

  List<Class<?>> type(K key);

  List<Class<?>> type(K key, Path2 path);

  List<Long> strAppend(K key, Path2 path, Object object);

  List<Long> strLen(K key, Path2 path);

  List<Long> arrAppend(K key, Path2 path, Object... objects);

  List<Long> arrIndex(K key, Path2 path, Object scalar);

  List<Long> arrInsert(K key, Path2 path, Integer index, Object... objects);

  List<Long> arrLen(K key, Path2 path);

  @Nullable
  <T> List<T> arrPop(K key, Class<T> clazz, Path2 path, Integer index);

  @Nullable
  <T> List<T> arrPop(K key, Class<T> clazz, Path2 path);

  @Nullable
  <T> List<T> arrPop(K key, Class<T> clazz);

  List<Long> arrTrim(K key, Path2 path, Integer start, Integer stop);

  void toggle(K key, Path2 path);

  List<Double> numIncrBy(K key, Path2 path, Long value);
}
