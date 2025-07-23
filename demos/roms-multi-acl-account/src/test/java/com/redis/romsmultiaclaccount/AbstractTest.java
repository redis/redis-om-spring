package com.redis.romsmultiaclaccount;

import com.redis.testcontainers.RedisContainer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.redis.testcontainers.RedisContainer.DEFAULT_IMAGE_NAME;

import org.testcontainers.utility.MountableFile;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext
public abstract class AbstractTest {

  @Container
  static final RedisContainer REDIS;

  static {
    REDIS = new RedisContainer(DEFAULT_IMAGE_NAME)
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("redis_acl.conf"),
                    "/usr/local/etc/redis/redis.conf"
            )
            .withCommand("redis-server", "/usr/local/etc/redis/redis.conf")
            .withExposedPorts(6379)
            .withReuse(true);

    REDIS.start();
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
  }
}