package com.redislabs.spring.annotations.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
import com.redislabs.spring.annotations.document.fixtures.MyDoc;
import com.redislabs.spring.annotations.document.fixtures.MyDocRepository;

@SpringBootTest(classes = RedisDocumentSearchTest.Config.class, properties = {"spring.main.allow-bean-definition-overriding=true"})
//@TestPropertySource(properties = "debug=true")
public class RedisDocumentSearchTest {

  @Autowired MyDocRepository repository;

  @Test
  public void testBasicCrudOperations() {
    MyDoc doc1 = MyDoc.of("hello world");
    
    Set<String> tags = new HashSet<String>();
    tags.add("news");
    tags.add("article");
    
    doc1.setTag(tags);
    doc1 = repository.save(doc1);
    assertEquals(1, repository.count());
    
    Optional<MyDoc> maybeDoc1 = repository.findById(doc1.getId());
    assertTrue(maybeDoc1.isPresent());
   
    assertEquals(doc1, maybeDoc1.get());
  }
  
  @Test
  public void testCustomFinder() {
    Optional<MyDoc> maybeDoc1 = repository.findByTitle("hello world");
    assertTrue(maybeDoc1.isPresent());
    System.out.println(">>>> maybeDoc1 " + maybeDoc1.get());
  }
  

  /**
   * > FT.SEARCH idx '@title:hello @tag:{news}' 
   * 1) (integer) 1 2) "doc1" 3) 1) "$"
   * 2) "{\"title\":\"hello world\",\"tag\":[\"news\",\"article\"]}"
   */
  @Test
  public void testRediSearchQuery01() {
/*
    SearchResult result = ops.search(new Query("@title:hello @tag:{news}"));
    assertEquals(1, result.totalResults);
    Document doc = result.docs.get(0);
    assertEquals(1.0, doc.getScore(), 0);
    assertNull(doc.getPayload());
    assertEquals("{\"title\":\"hello world\",\"tag\":[\"news\",\"article\"]}", doc.get("$"));
 */
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
