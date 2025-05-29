package com.redis.om.spring.client;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.RedisOMProperties;

import redis.clients.jedis.*;
import redis.clients.jedis.bloom.commands.BloomFilterCommands;
import redis.clients.jedis.bloom.commands.CountMinSketchCommands;
import redis.clients.jedis.bloom.commands.CuckooFilterCommands;
import redis.clients.jedis.bloom.commands.TDigestSketchCommands;
import redis.clients.jedis.bloom.commands.TopKFilterCommands;
import redis.clients.jedis.json.commands.RedisJsonCommands;
import redis.clients.jedis.search.RediSearchCommands;

/**
 * A unified client for accessing Redis Stack modules including RediSearch, RedisJSON,
 * and Redis Bloom (probabilistic data structures).
 * <p>
 * This client provides a centralized way to interact with various Redis modules through
 * the Jedis library. It supports both standalone and Sentinel deployment modes and
 * automatically configures the appropriate connection type based on the provided
 * {@link JedisConnectionFactory}.
 * <p>
 * The client offers convenient access methods for:
 * <ul>
 * <li>RedisJSON commands for JSON document operations</li>
 * <li>RediSearch commands for full-text search and indexing</li>
 * <li>Bloom Filter commands for probabilistic membership tests</li>
 * <li>Count-Min Sketch commands for frequency estimation</li>
 * <li>Cuckoo Filter commands for approximate set membership</li>
 * <li>TopK commands for tracking top-k items</li>
 * <li>T-Digest commands for quantile estimation</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class RedisModulesClient {
  private static final Log logger = LogFactory.getLog(RedisModulesClient.class);
  private final GsonBuilder builder;
  private final UnifiedJedis unifiedJedis;
  private final JedisConnectionFactory jedisConnectionFactory;

  /**
   * Constructs a new RedisModulesClient.
   *
   * @param jedisConnectionFactory the Jedis connection factory used to create connections to Redis
   * @param builder                the Gson builder for JSON serialization/deserialization operations
   */
  public RedisModulesClient(JedisConnectionFactory jedisConnectionFactory, GsonBuilder builder) {
    this.jedisConnectionFactory = jedisConnectionFactory;
    this.builder = builder;
    this.unifiedJedis = getUnifiedJedis();
  }

  private static HostAndPort apply(RedisNode node) {
    return new HostAndPort(node.getHost(), node.getPort() != null ? node.getPort() : 0);
  }

  /**
   * Returns a client for executing RedisJSON commands.
   * <p>
   * RedisJSON allows storing, updating, and retrieving JSON values in Redis keys.
   * This client provides access to all JSON operations including path-based queries,
   * atomic updates, and JSON transformations.
   *
   * @return a RedisJsonCommands instance for JSON operations
   */
  public RedisJsonCommands clientForJSON() {
    return unifiedJedis;
  }

  /**
   * Returns a client for executing RediSearch commands.
   * <p>
   * RediSearch provides full-text search capabilities, secondary indexing,
   * and query engine functionality. This client enables creating search indexes,
   * performing complex queries, and managing search operations.
   *
   * @return a RediSearchCommands instance for search operations
   */
  public RediSearchCommands clientForSearch() {
    return unifiedJedis;
  }

  /**
   * Returns a client for executing Bloom Filter commands.
   * <p>
   * Bloom Filters are space-efficient probabilistic data structures used to test
   * whether an element is a member of a set. False positive matches are possible,
   * but false negatives are not.
   *
   * @return a BloomFilterCommands instance for Bloom Filter operations
   */
  public BloomFilterCommands clientForBloom() {
    return unifiedJedis;
  }

  /**
   * Returns a client for executing Count-Min Sketch commands.
   * <p>
   * Count-Min Sketch is a probabilistic data structure that serves as a frequency table
   * of events in a stream of data. It can estimate the frequency of items with
   * configurable accuracy and probability guarantees.
   *
   * @return a CountMinSketchCommands instance for Count-Min Sketch operations
   */
  public CountMinSketchCommands clientForCMS() {
    return unifiedJedis;
  }

  /**
   * Returns a client for executing Cuckoo Filter commands.
   * <p>
   * Cuckoo Filters are probabilistic data structures that support approximate
   * set membership queries. They offer better space efficiency than Bloom Filters
   * and support deletion operations.
   *
   * @return a CuckooFilterCommands instance for Cuckoo Filter operations
   */
  public CuckooFilterCommands clientForCuckoo() {
    return unifiedJedis;
  }

  /**
   * Returns a client for executing TopK commands.
   * <p>
   * TopK maintains a list of k most frequent items in a stream. It provides
   * an efficient way to track the most popular items without storing the
   * entire frequency distribution.
   *
   * @return a TopKFilterCommands instance for TopK operations
   */
  public TopKFilterCommands clientForTopK() {
    return unifiedJedis;
  }

  /**
   * Returns a client for executing T-Digest commands.
   * <p>
   * T-Digest is a probabilistic data structure for estimating quantiles
   * from streaming or distributed data. It provides accurate quantile
   * estimates with configurable precision.
   *
   * @return a TDigestSketchCommands instance for T-Digest operations
   */
  public TDigestSketchCommands clientForTDigest() {
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
      var sentinels = sentinelConfiguration.getSentinels().stream().map(RedisModulesClient::apply).collect(Collectors
          .toSet());
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

  /**
   * Attempts to retrieve the underlying Jedis connection if available.
   * <p>
   * This method is useful when direct access to the Jedis connection is needed
   * for operations not covered by the module-specific clients. Returns an empty
   * Optional if the connection is not a standard Jedis instance.
   *
   * @return an Optional containing the Jedis connection, or empty if not available
   */
  public Optional<Jedis> getJedis() {
    Object nativeConnection = jedisConnectionFactory.getConnection().getNativeConnection();
    if (nativeConnection instanceof Jedis jedis) {
      return Optional.of(jedis);
    } else {
      return Optional.empty();
    }
  }

  /**
   * Attempts to retrieve the underlying JedisCluster connection if available.
   * <p>
   * This method provides access to the cluster connection when Redis is deployed
   * in cluster mode. Returns an empty Optional if the connection is not a
   * JedisCluster instance.
   *
   * @return an Optional containing the JedisCluster connection, or empty if not available
   */
  public Optional<JedisCluster> getJedisCluster() {
    Object nativeConnection = jedisConnectionFactory.getConnection().getNativeConnection();
    if (nativeConnection instanceof JedisCluster jedisCluster) {
      return Optional.of(jedisCluster);
    } else {
      return Optional.empty();
    }
  }

  /**
   * Returns the configured Gson builder for JSON serialization operations.
   * <p>
   * This builder is pre-configured with any custom serializers, deserializers,
   * and other settings needed for proper JSON handling within the Redis OM framework.
   *
   * @return the configured GsonBuilder instance
   */
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

    jedisConfigBuilder.clientSetInfoConfig(ClientSetInfoConfig.withLibNameSuffix(
        "redis-om-spring_v" + RedisOMProperties.ROMS_VERSION));

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
