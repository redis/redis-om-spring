package com.redislabs.spring.annotations.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import javax.annotation.PreDestroy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.test.context.TestPropertySource;

import com.redislabs.spring.annotations.EnableRedisDocumentRepositories;
import com.redislabs.spring.annotations.document.fixtures.Company;
import com.redislabs.spring.annotations.document.fixtures.CompanyRepository;

@SpringBootTest(classes = BasicRedisDocumentMappingTest.Config.class, properties = {"spring.main.allow-bean-definition-overriding=true"})
//@TestPropertySource(properties = "debug=true")
public class BasicRedisDocumentMappingTest {

  @Autowired CompanyRepository repository;

  @Test
  public void testBasicCrudOperations() {
    Company redislabs = repository.save(Company.of("RedisLabs"));
    Company microsoft = repository.save(Company.of("Microsoft"));
    
    assertEquals(2, repository.count());
    
    Optional<Company> maybeRedisLabs = repository.findById(redislabs.getId());
    Optional<Company> maybeMicrosoft = repository.findById(microsoft.getId());
    
    assertTrue(maybeRedisLabs.isPresent());
    assertTrue(maybeMicrosoft.isPresent());
    
    assertEquals(redislabs, maybeRedisLabs.get());
    assertEquals(microsoft, maybeMicrosoft.get());
  }

  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(basePackages="com.redislabs.spring.annotations.document.fixtures")
  static class Config {
    @Autowired
    RedisConnectionFactory connectionFactory;

    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
      RedisTemplate<?, ?> template = new RedisTemplate<>();
      template.setConnectionFactory(connectionFactory);

      return template;
    }
    
    @PreDestroy
    void cleanUp() {
      connectionFactory.getConnection().flushAll();
    }
  }
}
