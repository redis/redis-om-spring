package com.redislabs.spring.annotations.document;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.annotation.PreDestroy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.redislabs.spring.annotations.EnableRedisDocumentRepositories;
import com.redislabs.spring.annotations.document.fixtures.Company;
import com.redislabs.spring.annotations.document.fixtures.CompanyRepository;
import com.redislabs.spring.ops.RedisModulesOperations;

@SpringBootTest(classes = BasicRedisDocumentMappingTest.Config.class)
public class BasicRedisDocumentMappingTest {

  @Autowired
  RedisModulesOperations<String, String> modulesOperations;
  
  @Autowired CompanyRepository repository;

  @Test
  public void testBasicCrudOperations() {
    Company redislabs = Company.of("RedisLabs");
    Company microsoft = Company.of("Microsoft");
    repository.save(redislabs);
    repository.save(microsoft);
    
    assertEquals(2, repository.count());
  }

  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories
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
      //connectionFactory.getConnection().flushAll();
    }
  }
}
