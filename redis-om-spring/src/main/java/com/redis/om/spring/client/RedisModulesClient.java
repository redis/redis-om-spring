package com.redis.om.spring.client;

import java.util.Optional;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.google.gson.GsonBuilder;
//import com.redislabs.modules.rejson.JReJSON;
//
//import redis.clients.jedis.Jedis;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.bloom.commands.BloomFilterCommands;
import redis.clients.jedis.bloom.commands.CountMinSketchCommands;
import redis.clients.jedis.bloom.commands.CuckooFilterCommands;
import redis.clients.jedis.bloom.commands.TopKFilterCommands;
import redis.clients.jedis.json.RedisJsonCommands;
import redis.clients.jedis.search.RediSearchCommands;

public class RedisModulesClient {

  private GsonBuilder builder;

  public RedisModulesClient(JedisConnectionFactory jedisConnectionFactory, GsonBuilder builder) {
    this.jedisConnectionFactory = jedisConnectionFactory;
    this.builder = builder;
  }

  public RedisJsonCommands clientForJSON() {
    // JReJSON client = new JReJSON(getJedis());
    // client.setGsonBuilder(builder);
    // return client;
    return getUnifiedJedis().get();
  }

  public RediSearchCommands clientForSearch(String index) {
    // return new redis.clients.jedis.search.client.Client(index, getJedis());
    return getUnifiedJedis().get();
  }

  public BloomFilterCommands clientForBloom() {
    // return new io.rebloom.client.Client(getJedis());
    return getUnifiedJedis().get();
  }

  public CountMinSketchCommands clientForCMS() {
    return getUnifiedJedis().get();
  }

  public CuckooFilterCommands clientForCuckoo() {
    return getUnifiedJedis().get();
  }

  public TopKFilterCommands clientForTopK() {
    return getUnifiedJedis().get();
  }
  
  public Optional<UnifiedJedis> getUnifiedJedis() {
    Object nativeConnection = jedisConnectionFactory.getConnection().getNativeConnection();
    if (nativeConnection instanceof Jedis) {
      Jedis jedis = (Jedis)nativeConnection;
      return Optional.of(new UnifiedJedis(jedis.getConnection()));
    } else if (nativeConnection instanceof JedisCluster) {
      JedisCluster jedisCluster = (JedisCluster)nativeConnection;
      return Optional.of(new UnifiedJedis(jedisCluster.getConnectionFromSlot(0)));
    } else {
      return Optional.empty();
    }
  }

  public Optional<Jedis> getJedis() {
    Object nativeConnection = jedisConnectionFactory.getConnection().getNativeConnection();
    if (nativeConnection instanceof Jedis) {
      Jedis jedis = (Jedis)nativeConnection;
      return Optional.of(jedis);
    } else {
      return Optional.empty();
    }
  }
  
  public Optional<JedisCluster> getJedisCluster() {
    Object nativeConnection = jedisConnectionFactory.getConnection().getNativeConnection();
    if (nativeConnection instanceof JedisCluster) {
      JedisCluster jedisCluster = (JedisCluster)nativeConnection;
      return Optional.of(jedisCluster);
    } else {
      return Optional.empty();
    }
  }

  private JedisConnectionFactory jedisConnectionFactory;
}
