package com.redis.om.spring.annotations.document;

import com.google.common.collect.Sets;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import redis.clients.jedis.json.Path;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
