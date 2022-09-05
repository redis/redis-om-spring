package com.redis.om.spring.ops.json;

import java.util.List;
import org.springframework.lang.Nullable;
import redis.clients.jedis.json.JsonSetParams;
import redis.clients.jedis.json.Path;

public interface JSONOperations<K> {
  long del(K key, Path path);

  @Nullable
  Object get(K key);

  @Nullable
  <T> T get(K key, Class<T> clazz, Path... paths);

  @SuppressWarnings("unchecked")
  <T> List<T> mget(Class<T> clazz, K... keys);
  
  @SuppressWarnings("unchecked")
  <T> List<T> mget(Path path, Class<T> clazz, K... keys);
  
  String set(K key, Object object, JsonSetParams flag);

  String set(K key, Object object);

  String set(K key, Object object, Path path);

  String set(K key, Object object, JsonSetParams flag, Path path);
  
  Class<?> type(K key);
  
  Class<?> type(K key, Path path);
  
  Long strAppend(K key, Path path, Object... objects);
  
  Long strLen(K key, Path path);
  
  Long arrAppend(K key, Path path, Object... objects);
  
  Long arrIndex(K key, Path path, Object scalar);
  
  Long arrInsert(K key, Path path, int index, Object... objects);
  
  Long arrLen(K key, Path path);
  
  @Nullable
  <T> T arrPop(K key, Class<T> clazz, Path path, int index);
  
  @Nullable
  <T> T arrPop(K key, Class<T> clazz, Path path);
  
  @Nullable
  <T> T arrPop(K key, Class<T> clazz);
  
  Long arrTrim(K key, Path path, int start, int stop);

  String toggle(K key, Path path);

  Long numIncrBy(K key, Path path, Long value);

}
