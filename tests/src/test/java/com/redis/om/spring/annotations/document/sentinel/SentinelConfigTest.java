package com.redis.om.spring.annotations.document.sentinel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.mock.env.MockEnvironment;

import com.redis.om.spring.SentinelConfig;

/**
 * Tests for the SentinelConfig class, which provides Redis Sentinel connection
 * configuration for Redis OM Spring.
 * 
 * This test focuses on unit testing the SentinelConfig's configuration methods
 * without requiring actual Sentinel instances.
 */
@DisabledIfEnvironmentVariable(
    named = "GITHUB_ACTIONS", matches = "true"
)
@ExtendWith(
  MockitoExtension.class
)
class SentinelConfigTest {

  private SentinelConfig sentinelConfig;

  @BeforeEach
  void setUp() {
    sentinelConfig = new SentinelConfig();
  }

  @Test
  void testSentinelConfig() {
    assertNotNull(sentinelConfig);
  }

  @Test
  void testManualSentinelConfiguration() {
    // Create a mock environment with Sentinel properties
    MockEnvironment mockEnv = new MockEnvironment();
    mockEnv.setProperty("spring.redis.sentinel.master", "mymaster");
    mockEnv.setProperty("spring.redis.sentinel.nodes", "sentinel1:26379,sentinel2:26379,sentinel3:26379");
    mockEnv.setProperty("spring.data.redis.client-type", "jedis");

    // Create the factory using our mock environment
    JedisConnectionFactory factory = sentinelConfig.jedisConnectionFactory(mockEnv);

    // Verify the factory was created correctly
    assertNotNull(factory);

    // Get the Sentinel configuration from the factory
    RedisSentinelConfiguration sentinelConfiguration = (RedisSentinelConfiguration) factory.getSentinelConfiguration();

    assertThat(sentinelConfiguration).isNotNull();
    assertThat(sentinelConfiguration.getMaster().getName()).isEqualTo("mymaster");
    assertThat(sentinelConfiguration.getSentinels()).hasSize(3);

    // Verify the sentinel nodes
    Set<RedisNode> sentinels = sentinelConfiguration.getSentinels();
    boolean hasSentinel1 = false;
    boolean hasSentinel2 = false;
    boolean hasSentinel3 = false;

    for (RedisNode node : sentinels) {
      if (node.getHost().equals("sentinel1") && node.getPort() == 26379) {
        hasSentinel1 = true;
      } else if (node.getHost().equals("sentinel2") && node.getPort() == 26379) {
        hasSentinel2 = true;
      } else if (node.getHost().equals("sentinel3") && node.getPort() == 26379) {
        hasSentinel3 = true;
      }
    }

    assertThat(hasSentinel1).isTrue();
    assertThat(hasSentinel2).isTrue();
    assertThat(hasSentinel3).isTrue();
  }
}