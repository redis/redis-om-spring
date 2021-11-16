package com.redis.om.spring.client;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.google.gson.GsonBuilder;
import com.redislabs.modules.rejson.JReJSON;

import redis.clients.jedis.Jedis;

public class RedisModulesClient {

  public RedisModulesClient(JedisConnectionFactory jedisConnectionFactory) {
    this.jedisConnectionFactory = jedisConnectionFactory;
  }

  public JReJSON clientForJSON() {
    return new JReJSON(getJedis());
  };
  
  public JReJSON clientForJSON(GsonBuilder builder) {
    JReJSON client = new JReJSON(getJedis());
    client.setGsonBuilder(builder);
    return client;
  };

  public io.redisearch.Client clientForSearch(String index) {
    return new io.redisearch.client.Client(index, getJedis());
  }

  public io.rebloom.client.Client clientForBloom() {
    return new io.rebloom.client.Client(getJedis());
  }

  private Jedis getJedis() {
    return (Jedis) jedisConnectionFactory.getConnection().getNativeConnection();
  }

  private JedisConnectionFactory jedisConnectionFactory;
}
