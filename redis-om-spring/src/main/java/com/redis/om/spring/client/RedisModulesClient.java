package com.redis.om.spring.client;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.google.gson.GsonBuilder;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.bloom.RedisBloomCommands;
import redis.clients.jedis.json.RedisJsonCommands;
import redis.clients.jedis.search.RediSearchCommands;

public class RedisModulesClient {

  public RedisModulesClient(JedisConnectionFactory jedisConnectionFactory) {
    this.jedisConnectionFactory = jedisConnectionFactory;
  }

  public RedisJsonCommands clientForJSON() {
    return getUnifiedJedis();
  };
  
  public RedisJsonCommands clientForJSON(GsonBuilder builder) {
//    JReJSON client = new JReJSON(getJedis());
//    client.setGsonBuilder(builder);
//    return client;
    return getUnifiedJedis(); // TODO: set Gson
  };

  public RediSearchCommands clientForSearch() {
    return getUnifiedJedis();
  }

  public RedisBloomCommands clientForBloom() {
    return getUnifiedJedis();
  }

  public UnifiedJedis getUnifiedJedis() {
    return new UnifiedJedis(getJedis().getConnection());
  }

  public Jedis getJedis() {
    return (Jedis) jedisConnectionFactory.getConnection().getNativeConnection();
  }

  private JedisConnectionFactory jedisConnectionFactory;
}
