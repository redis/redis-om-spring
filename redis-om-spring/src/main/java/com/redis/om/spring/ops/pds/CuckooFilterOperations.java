package com.redis.om.spring.ops.pds;

import redis.clients.jedis.bloom.CFInsertParams;
import redis.clients.jedis.bloom.CFReserveParams;

import java.util.List;
import java.util.Map;

public interface CuckooFilterOperations<K> {
  void createFilter(String key, long capacity);
  void createFilter(String key, long capacity, CFReserveParams reserveParams);
  boolean add(String key, String item);
  boolean addNx(String key, String item);
  List<Boolean> insert(String key, String... items);
  List<Boolean> insert(String key, CFInsertParams insertParams, String... items);
  List<Boolean> insertNx(String key, String... items);
  List<Boolean> insertNx(String key, CFInsertParams insertParams, String... items);
  boolean exists(String key, String item);
  List<Boolean> exists(String key, String... items);
  boolean delete(String key, String item);
  long count(String key, String item);
  Map.Entry<Long, byte[]> scanDump(String key, long iterator);
  String loadChunk(String key, long iterator, byte[] data);
  Map<String, Object> info(String key);
}
