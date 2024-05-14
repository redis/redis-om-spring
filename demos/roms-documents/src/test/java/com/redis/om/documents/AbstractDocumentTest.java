package com.redis.om.documents;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DirtiesContext
@SpringBootTest(
  classes = AbstractDocumentTest.Config.class, properties = { "spring.main.allow-bean-definition-overriding=true" }
)
public abstract class AbstractDocumentTest extends AbstractTest {
  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories
  static class Config extends TestConfig {
  }
}
