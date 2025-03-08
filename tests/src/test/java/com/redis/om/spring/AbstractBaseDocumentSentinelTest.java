package com.redis.om.spring;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.ops.RedisModulesOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

@SuppressWarnings("resource")
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext
@SpringBootTest( //
                 classes = AbstractBaseDocumentSentinelTest.Config.class, //
                 properties = { "spring.main.allow-bean-definition-overriding=true" } //
                 )
@TestPropertySource(properties = { "spring.config.location=classpath:vss_on.yaml" })
public abstract class AbstractBaseDocumentSentinelTest {
  @Container
  public static final DockerComposeContainer<?> SENTINEL;
  protected static final int REDIS_PORT = 6379;
  protected static final int SENTINEL_PORT = 26379;
  protected static String dockerComposeFile = "sentinel/docker/docker-compose.yml";

  static {
    try {
      SENTINEL = new DockerComposeContainer<>(new ClassPathResource(dockerComposeFile).getFile()).withExposedService(
              "redis-master_1", REDIS_PORT, Wait.forListeningPort())
          .withExposedService("redis-sentinel_1", SENTINEL_PORT, Wait.forListeningPort());
      SENTINEL.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Autowired
  protected RedisModulesOperations<String> modulesOperations;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.redis.sentinel.master", () -> "mymaster");

    registry.add("spring.redis.sentinel.nodes",
        () -> SENTINEL.getServiceHost("redis-sentinel_1", SENTINEL_PORT) + ":" + SENTINEL.getServicePort(
            "redis-sentinel_1", SENTINEL_PORT));
  }

  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(
      basePackages = { "com.redis.om.spring.fixtures.document.model",
          "com.redis.om.spring.fixtures.document.repository", "com.redis.om.spring.repository" }
  )
  static class Config extends SentinelConfig {
  }
}