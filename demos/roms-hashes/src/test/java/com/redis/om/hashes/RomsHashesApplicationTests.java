package com.redis.om.hashes;

import static com.redis.testcontainers.RedisStackContainer.DEFAULT_IMAGE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.redis.om.hashes.domain.Role;
import com.redis.om.hashes.domain.User;
import com.redis.om.hashes.repositories.RoleRepository;
import com.redis.om.hashes.repositories.UserRepository;
import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;
import com.redis.testcontainers.RedisStackContainer;

import redis.clients.jedis.JedisPoolConfig;

/**
 * Regression test for gh-755: saving a @RedisHash entity whose Map<String,Object>
 * field contains nested Map/List values (as produced by Jackson deserialization)
 * must not throw a MappingException.
 */
@SpringBootTest(
    classes = RomsHashesApplicationTests.Config.class,
    properties = { "spring.main.allow-bean-definition-overriding=true" }
)
@Testcontainers(
    disabledWithoutDocker = true
)
@DirtiesContext
class RomsHashesApplicationTests {

  @Container
  static final RedisStackContainer REDIS;

  static {
    REDIS = new RedisStackContainer(DEFAULT_IMAGE_NAME.withTag("edge")).withReuse(true);
    REDIS.start();
  }

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
  }

  @SpringBootApplication
  @Configuration
  @EnableRedisEnhancedRepositories(
      basePackages = "com.redis.om.hashes.*"
  )
  static class Config {
    @Autowired
    Environment env;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
      String host = env.getProperty("spring.data.redis.host", "localhost");
      int port = env.getProperty("spring.data.redis.port", Integer.class, 6379);

      RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration(host, port);

      final JedisPoolConfig poolConfig = new JedisPoolConfig();
      poolConfig.setTestWhileIdle(false);
      poolConfig.setMinEvictableIdleDuration(Duration.ofMillis(60000));
      poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(30000));
      poolConfig.setNumTestsPerEvictionRun(-1);

      final JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder().connectTimeout(
          Duration.ofMillis(10000)).readTimeout(Duration.ofMillis(10000)).usePooling().poolConfig(poolConfig).build();

      return new JedisConnectionFactory(conf, jedisClientConfiguration);
    }

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
      StringRedisTemplate template = new StringRedisTemplate();
      template.setConnectionFactory(connectionFactory);
      return template;
    }
  }

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @Test
  void contextLoads() {
  }

  @Test
  void saveUserWithFlatMetadataDoesNotThrow() {
    Role role = roleRepository.save(Role.of("ADMIN"));
    User user = User.of("alice@example.com", "Alice", "Smith", role);
    user.setMetadata(Map.of("department", "engineering", "level", 3));

    assertThatNoException().isThrownBy(() -> userRepository.save(user));
  }

  /**
   * Reproduces gh-755: metadata value is itself a LinkedHashMap (typical output
   * from Jackson's ObjectMapper when deserializing into Map<String,Object>).
   * Previously threw: MappingException: Couldn't find PersistentEntity for type
   * class java.util.LinkedHashMap
   */
  @Test
  void saveUserWithNestedMapMetadataDoesNotThrow() {
    Role role = roleRepository.save(Role.of("ADMIN"));

    LinkedHashMap<String, Object> address = new LinkedHashMap<>();
    address.put("city", "Tel Aviv");
    address.put("zip", "6100000");

    User user = User.of("bob@example.com", "Bob", "Jones", role);
    user.setMetadata(Map.of("address", address, "score", 99));

    assertThatNoException().isThrownBy(() -> userRepository.save(user));
  }

  @Test
  void saveAllUsersWithNestedMapMetadataDoesNotThrow() {
    Role role = roleRepository.save(Role.of("USER"));

    LinkedHashMap<String, Object> prefs = new LinkedHashMap<>();
    prefs.put("theme", "dark");
    prefs.put("notifications", Map.of("email", true, "sms", false));

    User u1 = User.of("carol@example.com", "Carol", "White", role);
    u1.setMetadata(Map.of("preferences", prefs));

    User u2 = User.of("dave@example.com", "Dave", "Black", role);
    u2.setMetadata(Map.of("plain", "value"));

    assertThatNoException().isThrownBy(() -> userRepository.saveAll(List.of(u1, u2)));
    assertThat(userRepository.count()).isEqualTo(2);
  }
}
