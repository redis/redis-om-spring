package com.redis.om.spring.ops.json;

import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.serialization.gson.ReferenceAwareGsonBuilder;
import org.springframework.lang.Nullable;
import redis.clients.jedis.json.JsonSetParams;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.json.Path2;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class JSONOperationsImpl<K> implements JSONOperations<K> {

  private final ReferenceAwareGsonBuilder builder;
  final RedisModulesClient client;

  public JSONOperationsImpl(RedisModulesClient client, ReferenceAwareGsonBuilder builder) {
    this.client = client;
    this.builder = builder;
  }

  @Override
  public Long del(K key, Path path) {
    return client.clientForJSON().jsonDel(key.toString(), path);
  }

  @Nullable
  @Override
  public String get(K key) {
    return client.clientForJSON().jsonGetAsPlainString(key.toString(), Path.ROOT_PATH);
  }

  @Override
  public <T> T get(K key, Class<T> clazz) {
    builder.processEntity(clazz);
    return builder.gson().fromJson(client.clientForJSON().jsonGetAsPlainString(key.toString(), Path.ROOT_PATH), clazz);
  }

  @Override
  public <T> T get(K key, Class<T> clazz, Path path) {
    builder.processEntity(clazz);
    return builder.gson().fromJson(client.clientForJSON().jsonGetAsPlainString(key.toString(), path), clazz);
  }

  @SafeVarargs
  @Override
  public final List<String> mget(K... keys) {
    return client.clientForJSON().jsonMGet(getKeysAsString(keys))
        .stream()
        .filter(Objects::nonNull)
        .map(jsonArr -> jsonArr.get(0))
        .map(Object::toString)
        .toList();
  }

  @SafeVarargs @Override
  public final <T> List<T> mget(Class<T> clazz, K... keys) {
    builder.processEntity(clazz);
    return client.clientForJSON().jsonMGet(getKeysAsString(keys))
        .stream()
        .filter(Objects::nonNull)
        .map(jsonArr -> jsonArr.get(0))
        .map(Object::toString)
        .map(str -> builder.gson().fromJson(str, clazz))
        .toList();
  }

  @SafeVarargs @Override
  public final <T> List<T> mget(Path2 path, Class<T> clazz, K... keys) {
    builder.processEntity(clazz);
    return client.clientForJSON().jsonMGet(path, getKeysAsString(keys))
        .stream()
        .map(Object::toString)
        .map(str -> builder.gson().fromJson(str, clazz))
        .toList();
  }

  @Override
  public void set(K key, Object object, JsonSetParams flag) {
    client.clientForJSON().jsonSet(key.toString(), object, flag);
  }

  @Override
  public void set(K key, Object object) {
    client.clientForJSON().jsonSetWithPlainString(key.toString(), Path.ROOT_PATH, builder.gson().toJson(object));
  }

  @Override
  public void set(K key, Object object, Path path) {
    client.clientForJSON().jsonSetWithPlainString(key.toString(), path, builder.gson().toJson(object));
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
  public Long strAppend(K key, Path path, Object object) {
    return client.clientForJSON().jsonStrAppend(key.toString(), path, object);
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

  @SafeVarargs
  private String[] getKeysAsString(K... keys) {
    return Arrays.stream(keys).map(Object::toString).toArray(String[]::new);
  }

}