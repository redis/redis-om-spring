package com.redis.om.spring.ops.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.redis.om.spring.client.RedisModulesClient;
import org.json.JSONArray;
import org.springframework.lang.Nullable;
import redis.clients.jedis.json.JsonSetParams;
import redis.clients.jedis.json.Path2;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class JSONOperationsImpl<K> implements JSONOperations<K> {

  private final GsonBuilder builder;
  private final RedisModulesClient client;
  private Gson gson;

  public JSONOperationsImpl(RedisModulesClient client, GsonBuilder builder) {
    this.client = client;
    this.builder = builder;
  }

  @Override
  public Long del(K key, Path2 path) {
    return client.clientForJSON().jsonDel(key.toString(), path);
  }

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

  @Override
  public <T> T get(K key, Class<T> clazz) {
    return get(key, clazz, Path2.ROOT_PATH);
  }

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

  @SafeVarargs
  @Override
  public final List<String> mget(K... keys) {
    return (keys.length > 0) ?
        client.clientForJSON().jsonMGet(getKeysAsString(keys)).stream().filter(Objects::nonNull)
            .map(jsonArr -> jsonArr.get(0)).map(Object::toString).toList() :
        List.of();
  }

  @SafeVarargs
  @Override
  public final <T> List<T> mget(Class<T> clazz, K... keys) {
    Gson g = getGson();
    return (keys.length > 0) ?
        client.clientForJSON().jsonMGet(getKeysAsString(keys)).stream().filter(Objects::nonNull)
            .map(jsonArr -> jsonArr.get(0)).map(Object::toString).map(str -> g.fromJson(str, clazz)).toList() :
        List.of();
  }

  @SafeVarargs
  @Override
  public final <T> List<T> mget(Path2 path, Class<T> clazz, K... keys) {
    Gson g = getGson();
    return (keys.length > 0) ?
        client.clientForJSON().jsonMGet(path, getKeysAsString(keys)).stream().map(Object::toString)
            .map(str -> g.fromJson(str, clazz)).toList() :
        List.of();
  }

  @Override
  public void set(K key, Object object) {
    client.clientForJSON().jsonSet(key.toString(), Path2.ROOT_PATH, getGson().toJson(object));
  }

  @Override
  public void set(K key, Object object, Path2 path) {
    client.clientForJSON().jsonSet(key.toString(), path, getGson().toJson(object));
  }

  @Override
  public void set(K key, Object object, JsonSetParams params) {
    client.clientForJSON().jsonSet(key.toString(), object, params);
  }

  @Override
  public void set(K key, Object object, JsonSetParams params, Path2 path) {
    client.clientForJSON().jsonSet(key.toString(), path, object, params);
  }

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
    return client.clientForJSON().jsonArrPop(key.toString(), path, index).stream().map(Object::toString)
        .map(str -> getGson().fromJson(str, clazz)).toList();
  }

  @Override
  public <T> List<T> arrPop(K key, Class<T> clazz, Path2 path) {
    return client.clientForJSON().jsonArrPop(key.toString(), path).stream().map(Object::toString)
        .map(str -> getGson().fromJson(str, clazz)).toList();
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

  @SafeVarargs
  private String[] getKeysAsString(K... keys) {
    return Arrays.stream(keys).map(Object::toString).toArray(String[]::new);
  }

  @SuppressWarnings("unchecked")
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

  private Gson getGson() {
    if (gson == null) {
      gson = builder.create();
    }
    return gson;
  }
}