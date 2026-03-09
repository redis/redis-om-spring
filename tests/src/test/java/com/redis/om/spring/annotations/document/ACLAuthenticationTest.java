package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.fixtures.document.model.Company;
import com.redis.om.spring.fixtures.document.repository.CompanyRepository;

/**
 * Integration test that verifies ACL username/password authentication works
 * through the production {@code RedisModulesConfiguration.jedisConnectionFactory()} path.
 * <p>
 * This test intentionally does NOT extend TestConfig or AbstractBaseOMTest, so no custom
 * JedisConnectionFactory bean is provided. This causes the {@code @ConditionalOnMissingBean}
 * on {@code RedisModulesConfiguration.jedisConnectionFactory()} to activate, creating the
 * connection factory from {@code spring.data.redis.*} properties — the same path end users use.
 */
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext
@SpringBootTest(
    classes = ACLAuthenticationTest.Config.class,
    properties = { "spring.main.allow-bean-definition-overriding=true" }
)
@TestPropertySource(properties = { "spring.config.location=classpath:vss_on.yaml" })
class ACLAuthenticationTest {

  private static final int REDIS_PORT = 6379;

  @Container
  static final GenericContainer<?> REDIS;

  static {
    REDIS = new GenericContainer<>(DockerImageName.parse("redis:latest"))
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("redis_acl.conf"),
            "/usr/local/etc/redis/redis.conf"
        )
        .withCommand("redis-server", "/usr/local/etc/redis/redis.conf")
        .withExposedPorts(REDIS_PORT)
        .withReuse(true);
    REDIS.start();
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
    registry.add("spring.data.redis.username", () -> "testuser");
    registry.add("spring.data.redis.password", () -> "testpassword");
  }

  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(
      basePackages = {
          "com.redis.om.spring.fixtures.document.model",
          "com.redis.om.spring.fixtures.document.repository"
      }
  )
  static class Config {
    // Intentionally does NOT extend TestConfig — no custom JedisConnectionFactory bean.
    // This lets RedisModulesConfiguration.jedisConnectionFactory() create the factory
    // using spring.data.redis.* properties (including username/password).

    // Provide a RedisTemplate named "redisTemplate" as required by @EnableRedisDocumentRepositories.
    // In production, Spring Boot auto-configuration creates this; in tests we need it explicitly
    // because auto-configuration ordering may not guarantee it.
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, String> redisTemplate(JedisConnectionFactory connectionFactory) {
      RedisTemplate<String, String> template = new RedisTemplate<>();
      template.setKeySerializer(new StringRedisSerializer());
      template.setDefaultSerializer(new StringRedisSerializer());
      template.setConnectionFactory(connectionFactory);
      return template;
    }
  }

  @Autowired
  CompanyRepository repository;

  @BeforeEach
  void setUp() {
    repository.deleteAll();

    Company redis = Company.of(
        "Redis", 2011, LocalDate.of(2021, 5, 1),
        new Point(-122.066540, 37.377690), "redis@redis.com"
    );
    redis.setTags(Set.of("fast", "nosql", "database"));
    redis.setPubliclyListed(false);

    Company microsoft = Company.of(
        "Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640680), "info@microsoft.com"
    );
    microsoft.setTags(Set.of("enterprise", "software"));
    microsoft.setPubliclyListed(true);

    repository.saveAll(List.of(redis, microsoft));
  }

  @Test
  void testConnectionWithACLCredentials() {
    // Verify basic CRUD through a document repository works with ACL user
    Optional<Company> found = repository.findFirstByName("Redis");
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Redis");
    assertThat(found.get().getYearFounded()).isEqualTo(2011);
  }

  @Test
  void testSearchWithACLCredentials() {
    // Verify search/indexing works with ACL user
    List<Company> results = repository.findByName("Microsoft");
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getEmail()).isEqualTo("info@microsoft.com");

    // Verify tag-based query
    List<Company> publicCompanies = repository.findByPubliclyListed(true);
    assertThat(publicCompanies).hasSize(1);
    assertThat(publicCompanies.get(0).getName()).isEqualTo("Microsoft");
  }
}
