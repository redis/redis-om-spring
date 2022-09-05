package com.redis.om.spring.ops.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.Command;
import com.redis.om.spring.serialization.gson.GsonBuidlerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.json.JsonSetParams;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.util.SafeEncoder;

public class JSONOperationsImpl<K> implements JSONOperations<K> {

  RedisModulesClient client;
  GsonBuilder builder = GsonBuidlerFactory.getBuilder();

  public JSONOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }

  @Override
  public long del(K key, Path path) {
    return client.clientForJSON(builder).jsonDel(key.toString(), path);
  }

  @Override
  public Object get(K key) {
    return client.clientForJSON(builder).jsonGet(key.toString());
  }

  @Override
  public <T> T get(K key, Class<T> clazz, Path... paths) {
    return client.clientForJSON(builder).jsonGet(key.toString(), clazz, paths);
  }

  @Override
  public <T> List<T> mget(Class<T> clazz, @SuppressWarnings("unchecked") K... keys) {
    String[] keysAsStrings = Arrays.asList(keys).stream().map(Object::toString).toArray(String[]::new);
    return client.clientForJSON(builder).jsonMGet(clazz, keysAsStrings);
  }

  @Override
  public <T> List<T> mget(Path path, Class<T> clazz, @SuppressWarnings("unchecked") K... keys) {
    String[] keysAsStrings = Arrays.asList(keys).stream().map(Object::toString).toArray(String[]::new);
    return client.clientForJSON(builder).jsonMGet(path, clazz, keysAsStrings);
  }

  @Override
  public String set(K key, Object object, JsonSetParams flag) {
    return client.clientForJSON(builder).jsonSet(key.toString(), object, flag);
  }

  @Override
  public String set(K key, Object object) {
    return client.clientForJSON(builder).jsonSet(key.toString(), object);
  }

  @Override
  public String set(K key, Object object, Path path) {
    return client.clientForJSON(builder).jsonSet(key.toString(), path, object);
  }

  @Override
  public String set(K key, Object object, JsonSetParams flag, Path path) {
    return client.clientForJSON(builder).jsonSet(key.toString(), path, object, flag);
  }

  @Override
  public Class<?> type(K key) {
    return client.clientForJSON(builder).jsonType(key.toString());
  }

  @Override
  public Class<?> type(K key, Path path) {
    return client.clientForJSON(builder).jsonType(key.toString(), path);
  }

  @Override
  public Long strAppend(K key, Path path, Object... objects) {
    return client.clientForJSON(builder).jsonStrAppend(key.toString(), path, objects);
  }

  @Override
  public Long strLen(K key, Path path) {
    return client.clientForJSON(builder).jsonStrLen(key.toString(), path);
  }

  @Override
  public Long arrAppend(K key, Path path, Object... objects) {
    return client.clientForJSON(builder).jsonArrAppend(key.toString(), path, objects);
  }

  @Override
  public Long arrIndex(K key, Path path, Object scalar) {
    return client.clientForJSON(builder).jsonArrIndex(key.toString(), path, scalar);
  }

  @Override
  public Long arrInsert(K key, Path path, int index, Object... objects) {
    return client.clientForJSON(builder).jsonArrInsert(key.toString(), path, index, objects);
  }

  @Override
  public Long arrLen(K key, Path path) {
    return client.clientForJSON(builder).jsonArrLen(key.toString(), path);
  }

  @Override
  public <T> T arrPop(K key, Class<T> clazz, Path path, int index) {
    return client.clientForJSON(builder).jsonArrPop(key.toString(), clazz, path, index);
  }

  @Override
  public <T> T arrPop(K key, Class<T> clazz, Path path) {
    return client.clientForJSON(builder).jsonArrPop(key.toString(), clazz, path);
  }

  @Override
  public <T> T arrPop(K key, Class<T> clazz) {
    return client.clientForJSON(builder).jsonArrPop(key.toString(), clazz);
  }

  @Override
  public Long arrTrim(K key, Path path, int start, int stop) {
    return client.clientForJSON(builder).jsonArrTrim(key.toString(), path, start, stop);
  }

  @Override
  public String toggle(K key, Path path) {
    return client.clientForJSON(builder).jsonToggle(key.toString(), path);
  }

  // https://redis.io/commands/json.numincrby/
  @Override
  public Long numIncrBy(K key, Path path, Long value) {
    List<byte[]> args = new ArrayList<>();
    args.add(SafeEncoder.encode(key.toString()));
    args.add(SafeEncoder.encode(path != null ? path.toString() : Path.ROOT_PATH.toString()));
    args.add(Protocol.toByteArray(value));

    Long results = -1L;
    try (Jedis conn = client.getJedis()) {
      conn.getClient().sendCommand(Command.JSON_NUMINCRBY, args.toArray(new byte[args.size()][]));
      results = Long.parseLong(conn.getClient().getBulkReply());
    }
    
    return results;
  }

}
