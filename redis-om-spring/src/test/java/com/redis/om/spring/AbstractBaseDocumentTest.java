package com.redis.om.spring;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DirtiesContext
@SpringBootTest( //
    classes = AbstractBaseDocumentTest.Config.class, //
    properties = { "spring.main.allow-bean-definition-overriding=true" } //
)
public abstract class AbstractBaseDocumentTest extends AbstractBaseOMTest {
  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(basePackages = "com.redis.om.spring.annotations.document.fixtures")
  static class Config extends TestConfig {
  }
}
