package com.redis.om.spring.client;

import com.google.gson.GsonBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.connection.RedisNode;
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
import redis.clients.jedis.json.commands.RedisJsonCommands;
import redis.clients.jedis.search.RediSearchCommands;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class RedisModulesClient {
  private static final Log logger = LogFactory.getLog(RedisModulesClient.class);
  private final GsonBuilder builder;
  private final UnifiedJedis unifiedJedis;
  private final JedisConnectionFactory jedisConnectionFactory;

  public RedisModulesClient(JedisConnectionFactory jedisConnectionFactory, GsonBuilder builder) {
    this.jedisConnectionFactory = jedisConnectionFactory;
    this.builder = builder;
    this.unifiedJedis = getUnifiedJedis();
  }

  private static HostAndPort apply(RedisNode node) {
    return new HostAndPort(node.getHost(), node.getPort() != null ? node.getPort() : 0);
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

    var sentinelConfiguration = jedisConnectionFactory.getSentinelConfiguration();

    if (sentinelConfiguration != null) {
      //
      // Sentinel mode
      //
      var masterNode = sentinelConfiguration.getMaster();
      var master = masterNode != null ? masterNode.getName() : "mymaster";
      var sentinels = sentinelConfiguration.getSentinels().stream().map(RedisModulesClient::apply)
          .collect(Collectors.toSet());
      var password = sentinelConfiguration.getPassword();
      var sentinelPassword = sentinelConfiguration.getSentinelPassword();
      var username = sentinelConfiguration.getUsername();
      var masterClientConfig = createClientConfig(jedisConnectionFactory.getDatabase(), username, password,
          jedisConnectionFactory.getClientConfiguration());
      var sentinelClientConfig = createClientConfig(jedisConnectionFactory.getDatabase(), username, sentinelPassword,
          jedisConnectionFactory.getClientConfiguration());
      logger.info("Modules Client connecting in Sentinel mode, master: " + master);

      return new JedisSentineled(master, masterClientConfig, sentinels, sentinelClientConfig);

    } else {
      //
      // Standalone mode
      //
      var cc = jedisConnectionFactory.getClientConfiguration();
      var hostAndPort = new HostAndPort(jedisConnectionFactory.getHostName(), jedisConnectionFactory.getPort());
      var standaloneConfig = jedisConnectionFactory.getStandaloneConfiguration();
      var username = standaloneConfig != null ? standaloneConfig.getUsername() : null;
      var password = standaloneConfig != null ? standaloneConfig.getPassword() : null;
      var jedisClientConfig = createClientConfig(jedisConnectionFactory.getDatabase(), username, password, cc);

      logger.info("Modules Client connecting with standalone pool");

      return new JedisPooled(Objects.requireNonNull(jedisConnectionFactory.getPoolConfig()), hostAndPort,
          jedisClientConfig);
    }
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

  private JedisClientConfig createClientConfig(int database, @Nullable String username, RedisPassword password,
      JedisClientConfiguration clientConfiguration) {

    DefaultJedisClientConfig.Builder jedisConfigBuilder = DefaultJedisClientConfig.builder();

    clientConfiguration.getClientName().ifPresent(jedisConfigBuilder::clientName);
    jedisConfigBuilder.connectionTimeoutMillis(Math.toIntExact(clientConfiguration.getConnectTimeout().toMillis()));
    jedisConfigBuilder.socketTimeoutMillis(Math.toIntExact(clientConfiguration.getReadTimeout().toMillis()));

    jedisConfigBuilder.database(database);

    if (!ObjectUtils.isEmpty(username)) {
      jedisConfigBuilder.user(username);
    }
    password.toOptional().map(String::new).ifPresent(jedisConfigBuilder::password);

    if (clientConfiguration.isUseSsl()) {

      jedisConfigBuilder.ssl(true);

      clientConfiguration.getSslSocketFactory().ifPresent(jedisConfigBuilder::sslSocketFactory);
      clientConfiguration.getHostnameVerifier().ifPresent(jedisConfigBuilder::hostnameVerifier);
      clientConfiguration.getSslParameters().ifPresent(jedisConfigBuilder::sslParameters);
    }

    return jedisConfigBuilder.build();
  }
}
