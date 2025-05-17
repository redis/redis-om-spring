package com.redis.om.spring.search.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.TestResultRedisModel;
import com.redis.om.spring.fixtures.document.model.TestResultRedisModel$;
import com.redis.om.spring.fixtures.document.repository.TestResultRedisRepository;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;

class DialectSpaceFilterTest extends AbstractBaseDocumentTest {

  @Autowired
  TestResultRedisRepository testResultRedisRepository;

  @Autowired
  JedisConnectionFactory jedisConnectionFactory;

  @Autowired
  EntityStream es;

  private UnifiedJedis jedis;
  private static final String TEST_UUID = UUID.randomUUID().toString();

  @BeforeEach
  void cleanUp() {
    flushSearchIndexFor(TestResultRedisModel.class);

    if (testResultRedisRepository.count() == 0) {
      testResultRedisRepository.save(TestResultRedisModel.of(123L, TEST_UUID, "test-file.xml", "REJECTED"));
      testResultRedisRepository.save(TestResultRedisModel.of(456L, "456-456-456-456-456", "other-file.xml",
          "ACCEPTED"));
    }

    jedis = new JedisPooled(Objects.requireNonNull(jedisConnectionFactory.getPoolConfig()), jedisConnectionFactory
        .getHostName(), jedisConnectionFactory.getPort());
  }

  /**
   * This test verifies that count() can handle queries with spaces in filter conditions.
   * The issue was occurring in v0.9.11 when the DIALECT parameter was added to count queries.
   */
  @Test
  void testCountWithSpaceInFilterQuery() {
    // This simulates the same issue reported in GitHub issue #579
    // The filter creates a query with a space which combined with DIALECT 2 produces a syntax error
    final SearchStream<TestResultRedisModel> searchStream = es.of(TestResultRedisModel.class).filter(
        TestResultRedisModel$.UUID.eq(TEST_UUID));

    // The count operation was failing with: Syntax error at offset 16 near mapConfigId
    assertDoesNotThrow(() -> {
      long count = searchStream.count();
      assertThat(count).isEqualTo(1); // Should find one match
    });
  }

  /**
   * This test verifies that the count() method works correctly with multiple filter conditions
   * and properly handles the spaces between intersection conditions.
   */
  @Test
  void testCountWithMultipleFilterConditions() {
    // First add another test model with same status but different UUID
    testResultRedisRepository.save(TestResultRedisModel.of(789L, "another-uuid", "test-file.xml", "REJECTED"));

    // Create a search with multiple conditions (UUID AND status)
    final SearchStream<TestResultRedisModel> searchStream = es.of(TestResultRedisModel.class).filter(
        TestResultRedisModel$.UUID.eq(TEST_UUID)).filter(TestResultRedisModel$.STATUS.eq("REJECTED"));

    // This should return exactly one result that matches both conditions
    assertDoesNotThrow(() -> {
      long count = searchStream.count();
      assertThat(count).isEqualTo(1);
    });
  }
}