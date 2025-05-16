package com.redis.om.spring.annotations.hash;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.TestResultRedisModel;
import com.redis.om.spring.fixtures.hash.repository.TestResultRedisRepository;
import com.redis.om.spring.search.stream.EntityStream;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;

class DialectThreeTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  TestResultRedisRepository testResultRedisRepository;

  @Autowired
  JedisConnectionFactory jedisConnectionFactory;

  @Autowired
  EntityStream es;

  private UnifiedJedis jedis;

  @BeforeEach
  void cleanUp() {
    flushSearchIndexFor(TestResultRedisModel.class);

    if (testResultRedisRepository.count() == 0) {
      testResultRedisRepository.save(TestResultRedisModel.of(123L, "123-123-123-123-123",
          "9_TNR290INP\\-WEE2024011124\\.xml", "REJECTED"));
      testResultRedisRepository.save(TestResultRedisModel.of(456L, "456-456-456-456-456",
          "8_TNR290INP\\-WEE2024011124\\.xml", "ACCEPTED"));
    }

    jedis = new JedisPooled(Objects.requireNonNull(jedisConnectionFactory.getPoolConfig()), jedisConnectionFactory
        .getHostName(), jedisConnectionFactory.getPort());
  }

  @Test
  void testDialect3() {
    var results = testResultRedisRepository.findAllByFilenameIs("9_TNR290INP\\-WEE2024011124\\.xml");
    assertThat(results).hasSize(1);
    var result = results.iterator().next();
    assertThat(result.getUuid()).isEqualTo("123-123-123-123-123");
  }
}
