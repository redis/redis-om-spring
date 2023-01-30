package com.redis.om.spring.annotations;

import com.redis.om.spring.AbstractBaseOMTest;
import com.redis.om.spring.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest( //
    classes = DocRepositoriesAutoDiscoveryTest.Config.class, //
    properties = { "spring.main.allow-bean-definition-overriding=true" } //
)
class DocRepositoriesAutoDiscoveryTest extends AbstractBaseOMTest {
  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories()
  static class Config extends TestConfig {
  }

  @Autowired
  DocRepository repository;

  @Test
  void testBasePackageClassesAreFound() {
    Doc doc = repository.save(Doc.of("A Doc"));

    assertEquals(1, repository.count());

    Optional<Doc> maybeDoc = repository.findById(doc.getId());

    assertTrue(maybeDoc.isPresent());

    // delete given an id
    repository.deleteById(doc.getId());

    assertEquals(0, repository.count());
  }

}