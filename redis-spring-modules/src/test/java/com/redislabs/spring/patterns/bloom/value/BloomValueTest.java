package com.redislabs.spring.patterns.bloom.value;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.annotation.PreDestroy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import com.redislabs.spring.ops.RedisModulesOperations;
import com.redislabs.spring.patterns.bloom.value.fixtures.Person;
import com.redislabs.spring.patterns.bloom.value.fixtures.PersonRepository;

@SpringBootTest(classes = BloomValueTest.Config.class)
public class BloomValueTest {

  @Autowired
  RedisModulesOperations<String, String> modulesOperations;
  
  @Autowired PersonRepository repository;

  @Test
  public void testBasicCrudOperations() {
    Person guyr = Person.of("Guy Royse", "guy.royse@redislabs.com");
    Person guyk = Person.of("Guy Korland", "guy.korland@redislabs.com");
    Person simon = Person.of("Simon Prickett", "simon@redislabs.com");
    Person justin = Person.of("Justin Castilla", "justin@redislabs.com");
    Person steve = Person.of("Steve Loretto", "steve.lorello@redislabs.com");
    Person kyleo = Person.of("Kyle Owen", "kyle.owen@redislabs.com");
    Person kyleb = Person.of("Kyle Banker", "kyle.banker@redislabs.com");
    Person andrew = Person.of("Andrew Brookins", "andrew.brookins@redislabs.com");
    Person alex = Person.of("Oleksandr Korolko", "aleksandr@redislabs.com");
    Person lance = Person.of("Lance Leonard", "simon@redislabs.com");
    Person rachel = Person.of("Rachel Elledge", "simon@redislabs.com"); 
    Person kaitlyn = Person.of("Kaitlyn Michael", "simon@redislabs.com"); 
    Person josefin = Person.of("Josefin Sjoeberg", "josefin.sjoeberg@redislabs.com"); 
    List<Person> persons = List.of(guyr, guyk, simon, justin, steve, kyleo, kyleb, andrew, alex, lance, rachel, kaitlyn, josefin);
    
    repository.saveAll(persons);
    
    assertEquals(persons.size(), repository.count());
  }

  @SpringBootApplication
  @Configuration
  @EnableRedisRepositories
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
