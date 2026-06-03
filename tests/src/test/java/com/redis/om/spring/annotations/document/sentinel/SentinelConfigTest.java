package com.redis.om.spring.annotations.document.sentinel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.mock.env.MockEnvironment;

import com.redis.om.spring.RedisModulesConfiguration;
import com.redis.om.spring.SentinelConfig;

/**
 * Unit tests for sentinel node parsing in {@link RedisModulesConfiguration} and
 * {@link SentinelConfig}.
 *
 * <p>These tests use {@link MockEnvironment} only — no real Redis or Sentinel instance
 * is needed, so they run in every CI environment without restriction.
 */
@ExtendWith(
  MockitoExtension.class
)
class SentinelConfigTest {

  // ── RedisModulesConfiguration ──────────────────────────────────────────────

  @Test
  void redisModulesConfig_commaSeparatedNodes_parsedCorrectly() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("spring.data.redis.sentinel.master", "mymaster");
    env.setProperty("spring.data.redis.sentinel.nodes", "sentinel1:26379,sentinel2:26379,sentinel3:26379");

    JedisConnectionFactory factory = new RedisModulesConfiguration().jedisConnectionFactory(env);

    assertNotNull(factory);
    RedisSentinelConfiguration sentinelCfg = (RedisSentinelConfiguration) factory.getSentinelConfiguration();
    assertThat(sentinelCfg).isNotNull();
    assertThat(sentinelCfg.getMaster().getName()).isEqualTo("mymaster");
    assertSentinelNodes(sentinelCfg.getSentinels(), "sentinel1", "sentinel2", "sentinel3");
  }

  @Test
  void redisModulesConfig_yamlListNodes_parsedCorrectly() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("spring.data.redis.sentinel.master", "mymaster");
    // YAML list format — each element is registered as an indexed property
    env.setProperty("spring.data.redis.sentinel.nodes[0]", "sentinel1:26379");
    env.setProperty("spring.data.redis.sentinel.nodes[1]", "sentinel2:26379");
    env.setProperty("spring.data.redis.sentinel.nodes[2]", "sentinel3:26379");

    JedisConnectionFactory factory = new RedisModulesConfiguration().jedisConnectionFactory(env);

    assertNotNull(factory);
    RedisSentinelConfiguration sentinelCfg = (RedisSentinelConfiguration) factory.getSentinelConfiguration();
    assertThat(sentinelCfg).isNotNull();
    assertThat(sentinelCfg.getMaster().getName()).isEqualTo("mymaster");
    assertSentinelNodes(sentinelCfg.getSentinels(), "sentinel1", "sentinel2", "sentinel3");
  }

  @Test
  void redisModulesConfig_nodesWithoutPort_defaultSentinelPortApplied() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("spring.data.redis.sentinel.master", "mymaster");
    env.setProperty("spring.data.redis.sentinel.nodes", "sentinel1,sentinel2");

    JedisConnectionFactory factory = new RedisModulesConfiguration().jedisConnectionFactory(env);

    RedisSentinelConfiguration sentinelCfg = (RedisSentinelConfiguration) factory.getSentinelConfiguration();
    assertThat(sentinelCfg.getSentinels()).allSatisfy(
        node -> assertThat(node.getPort()).isEqualTo(RedisNode.DEFAULT_SENTINEL_PORT));
  }

  @Test
  void redisModulesConfig_noSentinelMaster_standaloneFactoryReturned() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("spring.data.redis.host", "redis-host");
    env.setProperty("spring.data.redis.port", "6380");

    JedisConnectionFactory factory = new RedisModulesConfiguration().jedisConnectionFactory(env);

    assertNotNull(factory);
    assertThat(factory.getSentinelConfiguration()).isNull();
    assertThat(factory.getStandaloneConfiguration()).isNotNull();
    assertThat(factory.getStandaloneConfiguration().getHostName()).isEqualTo("redis-host");
    assertThat(factory.getStandaloneConfiguration().getPort()).isEqualTo(6380);
  }

  // ── SentinelConfig ─────────────────────────────────────────────────────────

  @Test
  void sentinelConfig_commaSeparatedNodes_parsedCorrectly() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("spring.data.redis.sentinel.master", "mymaster");
    env.setProperty("spring.data.redis.sentinel.nodes", "sentinel1:26379,sentinel2:26379,sentinel3:26379");

    JedisConnectionFactory factory = new SentinelConfig().jedisConnectionFactory(env);

    assertNotNull(factory);
    RedisSentinelConfiguration sentinelCfg = (RedisSentinelConfiguration) factory.getSentinelConfiguration();
    assertThat(sentinelCfg).isNotNull();
    assertThat(sentinelCfg.getMaster().getName()).isEqualTo("mymaster");
    assertSentinelNodes(sentinelCfg.getSentinels(), "sentinel1", "sentinel2", "sentinel3");
  }

  @Test
  void sentinelConfig_yamlListNodes_parsedCorrectly() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("spring.data.redis.sentinel.master", "mymaster");
    env.setProperty("spring.data.redis.sentinel.nodes[0]", "sentinel1:26379");
    env.setProperty("spring.data.redis.sentinel.nodes[1]", "sentinel2:26379");
    env.setProperty("spring.data.redis.sentinel.nodes[2]", "sentinel3:26379");

    JedisConnectionFactory factory = new SentinelConfig().jedisConnectionFactory(env);

    assertNotNull(factory);
    RedisSentinelConfiguration sentinelCfg = (RedisSentinelConfiguration) factory.getSentinelConfiguration();
    assertThat(sentinelCfg).isNotNull();
    assertThat(sentinelCfg.getMaster().getName()).isEqualTo("mymaster");
    assertSentinelNodes(sentinelCfg.getSentinels(), "sentinel1", "sentinel2", "sentinel3");
  }

  @Test
  void sentinelConfig_nodesWithoutPort_defaultSentinelPortApplied() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("spring.data.redis.sentinel.master", "mymaster");
    env.setProperty("spring.data.redis.sentinel.nodes", "sentinel1,sentinel2");

    JedisConnectionFactory factory = new SentinelConfig().jedisConnectionFactory(env);

    RedisSentinelConfiguration sentinelCfg = (RedisSentinelConfiguration) factory.getSentinelConfiguration();
    assertThat(sentinelCfg.getSentinels()).allSatisfy(
        node -> assertThat(node.getPort()).isEqualTo(RedisNode.DEFAULT_SENTINEL_PORT));
  }

  // ── helpers ────────────────────────────────────────────────────────────────

  private void assertSentinelNodes(Set<RedisNode> nodes, String... expectedHosts) {
    assertThat(nodes).hasSize(expectedHosts.length);
    for (String host : expectedHosts) {
      assertThat(nodes).anyMatch(n -> n.getHost().equals(host) && n.getPort() == 26379);
    }
  }
}
