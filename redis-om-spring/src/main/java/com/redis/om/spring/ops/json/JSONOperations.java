package com.redis.om.spring.ops.json;

import java.util.List;

import org.springframework.lang.Nullable;

import com.redislabs.modules.rejson.Path;
import com.redislabs.modules.rejson.JReJSON.ExistenceModifier;

public interface JSONOperations<K> {
  Long del(K key, Path path);

  @Nullable
  <T> T get(K key);

  @Nullable
  <T> T get(K key, Class<T> clazz, Path... paths);

  @SuppressWarnings("unchecked")
  <T> List<T> mget(Class<T> clazz, K... keys);
  
  @SuppressWarnings("unchecked")
  <T> List<T> mget(Path path, Class<T> clazz, K... keys);
  
  void set(K key, Object object, ExistenceModifier flag);
  
  void set(K key, Object object);
  
  void set(K key, Object object, Path path);
  
  void set(K key, Object object, ExistenceModifier flag, Path path);
  
  Class<?> type(K key);
  
  Class<?> type(K key, Path path);
  
  Long strAppend(K key, Path path, Object... objects);
  
  Long strLen(K key, Path path);
  
  Long arrAppend(K key, Path path, Object... objects);
  
  Long arrIndex(K key, Path path, Object scalar);
  
  Long arrInsert(K key, Path path, Long index, Object... objects);
  
  Long arrLen(K key, Path path);
  
  @Nullable
  <T> T arrPop(K key, Class<T> clazz, Path path, Long index);
  
  @Nullable
  <T> T arrPop(K key, Class<T> clazz, Path path);
  
  @Nullable
  <T> T arrPop(K key, Class<T> clazz);
  
  Long arrTrim(K key, Path path, Long start, Long stop);

  void toggle(K key, Path path);

  Long numIncrBy(K key, Path path, Long value);

}
