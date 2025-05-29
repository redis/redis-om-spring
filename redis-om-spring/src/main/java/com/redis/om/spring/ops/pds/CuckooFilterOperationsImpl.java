package com.redis.om.spring.ops.pds;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.redis.om.spring.client.RedisModulesClient;

import redis.clients.jedis.bloom.CFInsertParams;
import redis.clients.jedis.bloom.CFReserveParams;

/**
 * Implementation of CuckooFilterOperations interface.
 * 
 * @param <K> the key type
 */
public class CuckooFilterOperationsImpl<K> implements CuckooFilterOperations<K> {
  final RedisModulesClient client;

  /**
   * Creates a new CuckooFilterOperationsImpl with the given Redis modules client.
   *
   * @param client the Redis modules client
   */
  public CuckooFilterOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }

  @Override
  public void createFilter(String key, long capacity) {
    client.clientForCuckoo().cfReserve(key, capacity);
  }

  @Override
  public void createFilter(String key, long capacity, CFReserveParams reserveParams) {
    client.clientForCuckoo().cfReserve(key, capacity, reserveParams);
  }

  @Override
  public boolean add(String key, String item) {
    return client.clientForCuckoo().cfAdd(key, item);
  }

  @Override
  public boolean addNx(String key, String item) {
    return client.clientForCuckoo().cfAddNx(key, item);
  }

  @Override
  public List<Boolean> insert(String key, String... items) {
    return client.clientForCuckoo().cfInsert(key, items);
  }

  @Override
  public List<Boolean> insert(String key, CFInsertParams insertParams, String... items) {
    return client.clientForCuckoo().cfInsert(key, insertParams, items);
  }

  @Override
  public List<Boolean> insertNx(String key, String... items) {
    return client.clientForCuckoo().cfInsertNx(key, items);
  }

  @Override
  public List<Boolean> insertNx(String key, CFInsertParams insertParams, String... items) {
    return client.clientForCuckoo().cfInsertNx(key, insertParams, items);
  }

  @Override
  public boolean exists(String key, String item) {
    return client.clientForCuckoo().cfExists(key, item);
  }

  @Override
  public List<Boolean> exists(String key, String... items) {
    return client.clientForCuckoo().cfMExists(key, items);
  }

  @Override
  public boolean delete(String key, String item) {
    return client.clientForCuckoo().cfDel(key, item);
  }

  @Override
  public long count(String key, String item) {
    return client.clientForCuckoo().cfCount(key, item);
  }

  @Override
  public Entry<Long, byte[]> scanDump(String key, long iterator) {
    return client.clientForCuckoo().cfScanDump(key, iterator);
  }

  @Override
  public String loadChunk(String key, long iterator, byte[] data) {
    return client.clientForCuckoo().cfLoadChunk(key, iterator, data);
  }

  @Override
  public Map<String, Object> info(String key) {
    return client.clientForCuckoo().cfInfo(key);
  }
}
