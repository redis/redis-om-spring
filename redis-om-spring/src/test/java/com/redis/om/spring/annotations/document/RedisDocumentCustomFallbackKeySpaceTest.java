package com.redis.om.spring.annotations.document;

import com.google.gson.JsonObject;
import com.redis.om.spring.AbstractBaseOMTest;
import com.redis.om.spring.TestConfig;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.annotations.document.fixtures.MyDoc;
import com.redis.om.spring.annotations.document.fixtures.MyDocRepository;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.json.Path;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DirtiesContext
@SpringBootTest( //
    classes = RedisDocumentCustomFallbackKeySpaceTest.Config.class, //
    properties = { "spring.main.allow-bean-definition-overriding=true" } //
) class RedisDocumentCustomFallbackKeySpaceTest extends AbstractBaseOMTest {

  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(basePackages = "com.redis.om.spring.annotations.document.fixtures")
  static class Config extends TestConfig {
    @Bean
    @Primary
    public RedisMappingContext keyValueMappingContext() {
      RedisMappingContext mappingContext = new RedisMappingContext();
      mappingContext.setFallbackKeySpaceResolver(type -> "CUSTOM_KEYSPACE" + ":" + type.getSimpleName());
      return mappingContext;
    }
  }


  @Autowired
  MyDocRepository myDocRepository;

  @Autowired
  RedisTemplate<String, String> template;

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  String myDoc1Id;

  @BeforeEach
  void loadTestData() {
    Point point1 = new Point(-122.124500, 47.640160);
    MyDoc myDoc1 = MyDoc.of("hello world", point1, point1, 1);

    Set<String> tags = new HashSet<>();
    tags.add("news");
    tags.add("article");

    myDoc1.setTag(tags);
    myDoc1 = myDocRepository.save(myDoc1);
    myDoc1Id = myDoc1.getId();

  }

  @AfterEach
  void cleanUp() {
    myDocRepository.deleteAll();
  }

  @Test
  void testSearchIndex() {
    SearchOperations<String> searchOps = modulesOperations.opsForSearch("com.redis.om.spring.annotations.document.fixtures.MyDocIdx");

    var info = searchOps.getInfo();

    var definition = info.get("index_definition");
    assertInstanceOf(List.class, definition);

    var prefixes = ((List<?>)definition).get(((List<?>)definition).indexOf("prefixes") + 1);
    assertNotNull(prefixes);
    assertInstanceOf(List.class, prefixes);
    assertEquals("CUSTOM_KEYSPACE:MyDoc:", ((List<?>)prefixes).get(0));
  }

  @Test
  void testModuleOperations() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();
    JsonObject rawJSON = ops.get("CUSTOM_KEYSPACE:MyDoc:" + myDoc1Id, JsonObject.class);

    assertNotNull(rawJSON);
    assertEquals(rawJSON.get("id").getAsString(), myDoc1Id);
  }

  @Test
  void testBasicCrudOperations() {
    assertEquals(1, myDocRepository.count());

    Optional<MyDoc> maybeDoc1 = myDocRepository.findById(myDoc1Id);
    assertTrue(maybeDoc1.isPresent());
    assertEquals("hello world", maybeDoc1.get().getTitle());

    MyDoc myDoc1 = maybeDoc1.get();
    myDoc1.setTitle(myDoc1.getTitle() + " updated");

    myDocRepository.save(myDoc1);
    maybeDoc1 = myDocRepository.findById(myDoc1Id);

    assertTrue(maybeDoc1.isPresent());
    assertEquals(myDoc1Id, maybeDoc1.get().getId());
    assertEquals("hello world updated", maybeDoc1.get().getTitle());

    myDocRepository.deleteById(myDoc1Id, Path.of("$.tag"));
    maybeDoc1 = myDocRepository.findById(myDoc1Id);

    assertTrue(maybeDoc1.isPresent());
    assertTrue(maybeDoc1.get().getTag().isEmpty());

    myDocRepository.deleteById(myDoc1Id);
    maybeDoc1 = myDocRepository.findById(myDoc1Id);

    assertFalse(maybeDoc1.isPresent());
  }
}
