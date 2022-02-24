package com.redis.om.spring.ops.pds;

import java.util.Map;

import com.redis.om.spring.client.RedisModulesClient;

import io.rebloom.client.InsertOptions;

public class BloomOperationsImpl<K> implements BloomOperations<K> {
  RedisModulesClient client;

  public BloomOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }

  @Override
  public void createFilter(K name, long initCapacity, double errorRate) {
    client.clientForBloom().createFilter(name.toString(), initCapacity, errorRate);
  }

  @Override
  public boolean add(K name, String value) {
    return client.clientForBloom().add(name.toString(), value);
  }

  @Override
  public boolean add(K name, byte[] value) {
    return client.clientForBloom().add(name.toString(), value);
  }

  @Override
  public boolean[] insert(K name, InsertOptions options, String... items) {
    return client.clientForBloom().insert(name.toString(), options, items);
  }

  @Override
  public boolean[] addMulti(K name, byte[]... values) {
    return client.clientForBloom().addMulti(name.toString(), values);
  }

  @Override
  public boolean[] addMulti(K name, String... values) {
    return client.clientForBloom().addMulti(name.toString(), values);
  }

  @Override
  public boolean exists(K name, String value) {
    return client.clientForBloom().exists(name.toString(), value);
  }

  @Override
  public boolean exists(K name, byte[] value) {
    return client.clientForBloom().exists(name.toString(), value);
  }

  @Override
  public boolean[] existsMulti(K name, byte[]... values) {
    return client.clientForBloom().existsMulti(name.toString(), values);
  }

  @Override
  public boolean[] existsMulti(K name, String... values) {
    return client.clientForBloom().existsMulti(name.toString(), values);
  }

  @Override
  public boolean delete(K name) {
    return client.clientForBloom().delete(name.toString());
  }

  @Override
  public Map<String, Object> info(K name) {
    return client.clientForBloom().info(name.toString());
  }

}
