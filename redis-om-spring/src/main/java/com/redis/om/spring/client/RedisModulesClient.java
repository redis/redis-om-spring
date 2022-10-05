package com.redis.om.spring.client;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.google.gson.GsonBuilder;
import com.redislabs.modules.rejson.JReJSON;

import redis.clients.jedis.Jedis;

public class RedisModulesClient {

  private GsonBuilder builder;

  public RedisModulesClient(JedisConnectionFactory jedisConnectionFactory, GsonBuilder builder) {
    this.jedisConnectionFactory = jedisConnectionFactory;
    this.builder = builder;
  }

  public JReJSON clientForJSON() {
    JReJSON client = new JReJSON(getJedis());
    client.setGsonBuilder(builder);
    return client;
  }

  public io.redisearch.Client clientForSearch(String index) {
    return new io.redisearch.client.Client(index, getJedis());
  }

  public io.rebloom.client.Client clientForBloom() {
    return new io.rebloom.client.Client(getJedis());
  }

  public Jedis getJedis() {
    return (Jedis) jedisConnectionFactory.getConnection().getNativeConnection();
  }

  private JedisConnectionFactory jedisConnectionFactory;
}
