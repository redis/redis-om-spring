package com.redis.om.spring;

import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

@SpringBootTest( //
    classes = AbstractBaseEnhancedRedisTest.Config.class, //
    properties = { "spring.main.allow-bean-definition-overriding=true" } //
)
public abstract class AbstractBaseEnhancedRedisTest extends AbstractBaseOMTest {
  @SpringBootApplication
  @Configuration
  @EnableRedisEnhancedRepositories(basePackages = { "com.redis.om.spring.annotations.hash.fixtures" })
  static class Config extends TestConfig {
  }
}
