package com.redis.om.spring.client;

import com.google.gson.GsonBuilder;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.*;
import redis.clients.jedis.bloom.commands.BloomFilterCommands;
import redis.clients.jedis.bloom.commands.CountMinSketchCommands;
import redis.clients.jedis.bloom.commands.CuckooFilterCommands;
import redis.clients.jedis.bloom.commands.TopKFilterCommands;
import redis.clients.jedis.json.RedisJsonCommands;
import redis.clients.jedis.search.RediSearchCommands;

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
    var cc = jedisConnectionFactory.getClientConfiguration();
    var hostAndPort = new HostAndPort(jedisConnectionFactory.getHostName(), jedisConnectionFactory.getPort());
    var jedisClientConfig = createClientConfig(jedisConnectionFactory.getDatabase(),
        jedisConnectionFactory.getStandaloneConfiguration().getUsername(),
        jedisConnectionFactory.getStandaloneConfiguration().getPassword(), cc);

    return new JedisPooled(Objects.requireNonNull(jedisConnectionFactory.getPoolConfig()), hostAndPort,
        jedisClientConfig);
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

  private JedisClientConfig createClientConfig(int database, @Nullable String username, RedisPassword password, JedisClientConfiguration clientConfiguration) {

    DefaultJedisClientConfig.Builder builder = DefaultJedisClientConfig.builder();

    clientConfiguration.getClientName().ifPresent(builder::clientName);
    builder.connectionTimeoutMillis(Math.toIntExact(clientConfiguration.getConnectTimeout().toMillis()));
    builder.socketTimeoutMillis(Math.toIntExact(clientConfiguration.getReadTimeout().toMillis()));

    builder.database(database);

    if (!ObjectUtils.isEmpty(username)) {
      builder.user(username);
    }
    password.toOptional().map(String::new).ifPresent(builder::password);

    if (clientConfiguration.isUseSsl()) {

      builder.ssl(true);

      clientConfiguration.getSslSocketFactory().ifPresent(builder::sslSocketFactory);
      clientConfiguration.getHostnameVerifier().ifPresent(builder::hostnameVerifier);
      clientConfiguration.getSslParameters().ifPresent(builder::sslParameters);
    }

    return builder.build();
  }
}
