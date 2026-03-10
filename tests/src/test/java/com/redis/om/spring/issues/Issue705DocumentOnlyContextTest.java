package com.redis.om.spring.issues;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.geo.Point;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.redis.om.spring.AbstractBaseOMTest;
import com.redis.om.spring.TestConfig;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.fixtures.document.model.MyDoc;
import com.redis.om.spring.fixtures.document.repository.MyDocRepository;

/**
 * Test for issue #705: Bean creation error for redisEnhancedMappingContext
 * when only @EnableRedisDocumentRepositories is used (without
 * @EnableRedisEnhancedRepositories).
 *
 * The root cause was RediSearchIndexer using service locator pattern
 * (ac.getBean()) in its constructor, which forced eager bean resolution
 * and broke initialization order in Spring Boot 4.0 / Spring Framework 7.0.
 */
@Testcontainers
@DirtiesContext
@SpringBootTest(classes = Issue705DocumentOnlyContextTest.Config.class)
class Issue705DocumentOnlyContextTest extends AbstractBaseOMTest {

  @Autowired
  MyDocRepository myDocRepository;

  String myDocId;

  @BeforeEach
  void loadTestData() {
    Point point = new Point(-122.124500, 47.640160);
    MyDoc myDoc = MyDoc.of("issue 705 test", point, point, 1);
    myDoc = myDocRepository.save(myDoc);
    myDocId = myDoc.getId();
  }

  @AfterEach
  void cleanUp() {
    myDocRepository.deleteAll();
  }

  @Test
  void testContextLoadsWithDocumentRepositoriesOnly() {
    // If we get here, the context started successfully - the core assertion
    assertThat(myDocRepository).isNotNull();
  }

  @Test
  void testDocumentSaveAndRetrieve() {
    Optional<MyDoc> maybeDoc = myDocRepository.findById(myDocId);
    assertTrue(maybeDoc.isPresent());
    assertEquals("issue 705 test", maybeDoc.get().getTitle());
  }

  @Test
  void testDocumentUpdate() {
    Optional<MyDoc> maybeDoc = myDocRepository.findById(myDocId);
    assertTrue(maybeDoc.isPresent());

    MyDoc doc = maybeDoc.get();
    doc.setTitle("updated title");
    myDocRepository.save(doc);

    maybeDoc = myDocRepository.findById(myDocId);
    assertTrue(maybeDoc.isPresent());
    assertEquals("updated title", maybeDoc.get().getTitle());
  }

  @Test
  void testDocumentDelete() {
    myDocRepository.deleteById(myDocId);
    Optional<MyDoc> maybeDoc = myDocRepository.findById(myDocId);
    assertFalse(maybeDoc.isPresent());
  }

  /**
   * Configuration that ONLY enables @EnableRedisDocumentRepositories
   * WITHOUT @EnableRedisEnhancedRepositories.
   * This mirrors the setup reported in issue #705.
   */
  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(
      basePackages = {
          "com.redis.om.spring.fixtures.document.model",
          "com.redis.om.spring.fixtures.document.repository"
      }
  )
  static class Config extends TestConfig {
  }
}
