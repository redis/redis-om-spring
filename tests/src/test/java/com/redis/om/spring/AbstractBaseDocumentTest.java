package com.redis.om.spring;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DirtiesContext
@SpringBootTest( //
                 classes = AbstractBaseDocumentTest.Config.class, //
                 properties = { "spring.main.allow-bean-definition-overriding=true" } //
                 )
@TestPropertySource(properties = { "spring.config.location=classpath:vss_on.yaml" })
public abstract class AbstractBaseDocumentTest extends AbstractBaseOMTest {
  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(
      basePackages = { "com.redis.om.spring.fixtures.document.model",
          "com.redis.om.spring.fixtures.document.repository", "com.redis.om.spring.repository" }
  )
  static class Config extends TestConfig {
  }
}
