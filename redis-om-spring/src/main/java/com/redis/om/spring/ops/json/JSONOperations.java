package com.redis.om.spring.ops.json;

import java.util.List;

import org.springframework.lang.Nullable;

import redis.clients.jedis.json.JsonSetParams;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.json.Path2;

public interface JSONOperations<K> {
  Long del(K key, Path path);

  @Nullable
  <T> T get(K key, Class<T> clazz);

  @Nullable
  <T> T get(K key, Class<T> clazz, Path path);

  @SuppressWarnings("unchecked")
  <T> List<T> mget(Class<T> clazz, K... keys);

  @SuppressWarnings("unchecked")
  <T> List<T> mget(Path2 path, Class<T> clazz, K... keys);

  void set(K key, Object object, JsonSetParams params);

  void set(K key, Object object);

  void set(K key, Object object, Path path);

  void set(K key, Object object, JsonSetParams params, Path path);

  Class<?> type(K key);

  Class<?> type(K key, Path path);

  Long strAppend(K key, Path path, Object object);

  Long strLen(K key, Path path);

  Long arrAppend(K key, Path path, Object... objects);

  Long arrIndex(K key, Path path, Object scalar);

  Long arrInsert(K key, Path path, Integer index, Object... objects);

  Long arrLen(K key, Path path);

  @Nullable
  <T> T arrPop(K key, Class<T> clazz, Path path, Integer index);

  @Nullable
  <T> T arrPop(K key, Class<T> clazz, Path path);

  @Nullable
  <T> T arrPop(K key, Class<T> clazz);

  Long arrTrim(K key, Path path, Integer start, Integer stop);

  void toggle(K key, Path path);

  Double numIncrBy(K key, Path path, Long value);
}
