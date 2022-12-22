package com.redis.om.spring.ops.json;

import java.util.Arrays;
import java.util.List;

import com.redis.om.spring.client.RedisModulesClient;

import redis.clients.jedis.json.JsonSetParams;
import redis.clients.jedis.json.Path;

public class JSONOperationsImpl<K> implements JSONOperations<K> {

  RedisModulesClient client;

  public JSONOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }

  @Override
  public Long del(K key, Path path) {
    return client.clientForJSON().jsonDel(key.toString(), path);
  }

  @Override
  public <T> T get(K key, Class<T> clazz) {
    return client.clientForJSON().jsonGet(key.toString(), clazz);
  }

  @Override
  public <T> T get(K key, Class<T> clazz, Path... paths) {
    return client.clientForJSON().jsonGet(key.toString(), clazz, paths);
  }

  @Override
  public <T> List<T> mget(Class<T> clazz, @SuppressWarnings("unchecked") K... keys) {
    String[] keysAsStrings = Arrays.asList(keys).stream().map(Object::toString).toArray(String[]::new);
    return client.clientForJSON().jsonMGet(clazz, keysAsStrings);
  }

  @Override
  public <T> List<T> mget(Path path, Class<T> clazz, @SuppressWarnings("unchecked") K... keys) {
    String[] keysAsStrings = Arrays.asList(keys).stream().map(Object::toString).toArray(String[]::new);
    return client.clientForJSON().jsonMGet(path, clazz, keysAsStrings);
  }

  @Override
  public void set(K key, Object object, JsonSetParams flag) {
    client.clientForJSON().jsonSet(key.toString(), object, flag);
  }

  @Override
  public void set(K key, Object object) {
    client.clientForJSON().jsonSet(key.toString(), object);
  }

  @Override
  public void set(K key, Object object, Path path) {
    client.clientForJSON().jsonSet(key.toString(), path, object);
  }

  @Override
  public void set(K key, Object object, JsonSetParams params, Path path) {
    client.clientForJSON().jsonSet(key.toString(), path, object, params);
  }

  @Override
  public Class<?> type(K key) {
    return client.clientForJSON().jsonType(key.toString());
  }

  @Override
  public Class<?> type(K key, Path path) {
    return client.clientForJSON().jsonType(key.toString(), path);
  }

  @Override
  public Long strAppend(K key, Path path, Object... objects) {
    return client.clientForJSON().jsonStrAppend(key.toString(), path, objects);
  }

  @Override
  public Long strLen(K key, Path path) {
    return client.clientForJSON().jsonStrLen(key.toString(), path);
  }

  @Override
  public Long arrAppend(K key, Path path, Object... objects) {
    return client.clientForJSON().jsonArrAppend(key.toString(), path, objects);
  }

  @Override
  public Long arrIndex(K key, Path path, Object scalar) {
    return client.clientForJSON().jsonArrIndex(key.toString(), path, scalar);
  }

  @Override
  public Long arrInsert(K key, Path path, Integer index, Object... objects) {
    return client.clientForJSON().jsonArrInsert(key.toString(), path, index, objects);
  }

  @Override
  public Long arrLen(K key, Path path) {
    return client.clientForJSON().jsonArrLen(key.toString(), path);
  }

  @Override
  public <T> T arrPop(K key, Class<T> clazz, Path path, Integer index) {
    return client.clientForJSON().jsonArrPop(key.toString(), clazz, path, index);
  }

  @Override
  public <T> T arrPop(K key, Class<T> clazz, Path path) {
    return client.clientForJSON().jsonArrPop(key.toString(), clazz, path);
  }

  @Override
  public <T> T arrPop(K key, Class<T> clazz) {
    return client.clientForJSON().jsonArrPop(key.toString(), clazz);
  }

  @Override
  public Long arrTrim(K key, Path path, Integer start, Integer stop) {
    return client.clientForJSON().jsonArrTrim(key.toString(), path, start, stop);
  }

  @Override
  public void toggle(K key, Path path) {
    client.clientForJSON().jsonToggle(key.toString(), path);
  }

  @Override
  public Double numIncrBy(K key, Path path, Long value) {
    return client.clientForJSON().jsonNumIncrBy(key.toString(), path, value);
  }

}
