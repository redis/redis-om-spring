package com.redis.om.spring.client;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.bloom.commands.BloomFilterCommands;
import redis.clients.jedis.bloom.commands.CountMinSketchCommands;
import redis.clients.jedis.bloom.commands.CuckooFilterCommands;
import redis.clients.jedis.bloom.commands.TopKFilterCommands;
import redis.clients.jedis.json.RedisJsonCommands;
import redis.clients.jedis.search.RediSearchCommands;
import com.google.gson.GsonBuilder;

import java.util.Objects;
import java.util.Optional;

public class RedisModulesClient {

  private final GsonBuilder builder;
  private final UnifiedJedis unifiedJedis;

  public RedisModulesClient(JedisConnectionFactory jedisConnectionFactory, GsonBuilder builder) {
    this.jedisConnectionFactory = jedisConnectionFactory;
    this.builder = builder;
    this.unifiedJedis = getUnifiedJedis();
  }

  public RedisJsonCommands clientForJSON() {
    return unifiedJedis;
  }

  public RediSearchCommands clientForSearch() {
    return unifiedJedis;
  }

  public BloomFilterCommands clientForBloom() {
    return unifiedJedis;
  }

  public CountMinSketchCommands clientForCMS() {
    return unifiedJedis;
  }

  public CuckooFilterCommands clientForCuckoo() {
    return unifiedJedis;
  }

  public TopKFilterCommands clientForTopK() {
    return unifiedJedis;
  }

  private UnifiedJedis getUnifiedJedis() {
    return new JedisPooled(Objects.requireNonNull(jedisConnectionFactory.getPoolConfig()),
        jedisConnectionFactory.getHostName(), jedisConnectionFactory.getPort());
  }

  public Optional<Jedis> getJedis() {
    Object nativeConnection = jedisConnectionFactory.getConnection().getNativeConnection();
    if (nativeConnection instanceof Jedis jedis) {
      return Optional.of(jedis);
    } else {
      return Optional.empty();
    }
  }
  
  public Optional<JedisCluster> getJedisCluster() {
    Object nativeConnection = jedisConnectionFactory.getConnection().getNativeConnection();
    if (nativeConnection instanceof JedisCluster jedisCluster) {
      return Optional.of(jedisCluster);
    } else {
      return Optional.empty();
    }
  }

  public GsonBuilder gsonBuilder() {
    return builder;
  }

  private final JedisConnectionFactory jedisConnectionFactory;
}
