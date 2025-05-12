package com.foogaro.modeling.config;

import com.foogaro.modeling.Utils;
import com.redis.testcontainers.RedisStackContainer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
@Testcontainers
public class TestRedisConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(TestRedisConfiguration.class);

    @Container
    public static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse(RedisStackContainer.DEFAULT_IMAGE_NAME.withTag(RedisStackContainer.DEFAULT_TAG).toString()))
            .withExposedPorts(Utils.REDIS_PORT);

    @PostConstruct
    public void init() {
        while (!redis.isRunning()) {
            logger.info("Waiting for Redis to start.");
            redis.start();
        }
        logger.info("Redis is running.");
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(Utils.REDIS_PORT));
    }

    @Bean
    @Primary
    public JedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
                redis.getHost(),
                redis.getMappedPort(Utils.REDIS_PORT)
        );
        return new JedisConnectionFactory(config);
    }
}