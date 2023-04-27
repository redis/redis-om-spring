package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.User2;
import com.redis.om.spring.annotations.document.fixtures.User2Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpellCheckingInspection")
class RepositoryIssuesTest extends AbstractBaseDocumentTest {
  @Autowired
  User2Repository repository;

  @BeforeEach
  void cleanUp() {
    repository.deleteAll();
    repository.save(User2.of("Doe", "Paris", "12 rue Rivoli"));
  }

  // RediSearchQuery wrong preparedQuery #187
  @Test
  void testIncorrectParameterInjection() {
    Iterable<User2> results = repository.findUser("Doe", "Paris", "12 rue Rivoli");

    assertThat(results).extracting("name").containsExactly("Doe");
  }
}
