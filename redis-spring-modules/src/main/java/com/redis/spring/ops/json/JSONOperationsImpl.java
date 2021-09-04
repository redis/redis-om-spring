package com.redis.spring.ops.json;

import java.util.Arrays;
import java.util.List;

import com.redislabs.modules.rejson.JReJSON.ExistenceModifier;
import com.redis.spring.client.RedisModulesClient;
import com.redislabs.modules.rejson.Path;

public class JSONOperationsImpl<K> implements JSONOperations<K> {
  
  RedisModulesClient client;

  public JSONOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }

  @Override
  public Long del(K key, Path path) {
    return client.clientForJSON().del(key.toString(), path);
  }

  @Override
  public <T> T get(K key) {
    return client.clientForJSON().get(key.toString());
  }

  @Override
  public <T> T get(K key, Class<T> clazz, Path... paths) {
    return client.clientForJSON().get(key.toString(), clazz, paths);
  }

  @Override
  public <T> List<T> mget(Class<T> clazz, @SuppressWarnings("unchecked") K... keys) {
    String[] keysAsStrings = Arrays.asList(keys).stream().map(Object::toString).toArray(String[]::new);
    return client.clientForJSON().mget(clazz, keysAsStrings);
  }

  @Override
  public <T> List<T> mget(Path path, Class<T> clazz, @SuppressWarnings("unchecked") K... keys) {
    String[] keysAsStrings = Arrays.asList(keys).stream().map(Object::toString).toArray(String[]::new);
    return client.clientForJSON().mget(path, clazz, keysAsStrings);
  }

  @Override
  public void set(K key, Object object, ExistenceModifier flag) {
    client.clientForJSON().set(key.toString(), object, flag);
  }

  @Override
  public void set(K key, Object object) {
    client.clientForJSON().set(key.toString(), object);
  }

  @Override
  public void set(K key, Object object, Path path) {
    client.clientForJSON().set(key.toString(), object, path);
  }

  @Override
  public void set(K key, Object object, ExistenceModifier flag, Path path) {
    client.clientForJSON().set(key.toString(), object, flag, path);
  }

  @Override
  public Class<?> type(K key) {
    return client.clientForJSON().type(key.toString());
  }

  @Override
  public Class<?> type(K key, Path path) {
    return client.clientForJSON().type(key.toString(), path);
  }

  @Override
  public Long strAppend(K key, Path path, Object... objects) {
    return client.clientForJSON().strAppend(key.toString(), path, objects);
  }

  @Override
  public Long strLen(K key, Path path) {
    return client.clientForJSON().strLen(key.toString(), path);
  }

  @Override
  public Long arrAppend(K key, Path path, Object... objects) {
    return client.clientForJSON().arrAppend(key.toString(), path, objects);
  }

  @Override
  public Long arrIndex(K key, Path path, Object scalar) {
    return client.clientForJSON().arrIndex(key.toString(), path, scalar);
  }

  @Override
  public Long arrInsert(K key, Path path, Long index, Object... objects) {
    return client.clientForJSON().arrInsert(key.toString(), path, index, objects);
  }

  @Override
  public Long arrLen(K key, Path path) {
    return client.clientForJSON().arrLen(key.toString(), path);
  }

  @Override
  public <T> T arrPop(K key, Class<T> clazz, Path path, Long index) {
    return client.clientForJSON().arrPop(key.toString(), clazz, path, index);
  }

  @Override
  public <T> T arrPop(K key, Class<T> clazz, Path path) {
    return client.clientForJSON().arrPop(key.toString(), clazz, path);
  }

  @Override
  public <T> T arrPop(K key, Class<T> clazz) {
    return client.clientForJSON().arrPop(key.toString(), clazz);
  }

  @Override
  public Long arrTrim(K key, Path path, Long start, Long stop) {
    return client.clientForJSON().arrTrim(key.toString(), path, start, stop);
  }

}
