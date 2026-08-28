package com.redis.om.spring.issues;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.fixtures.hash.model.Country;
import com.redis.om.spring.fixtures.hash.repository.CountryRepository;

/**
 * Regression test for issue #769, confirming the same
 * {@code NoSuchElementException: No value present} failure reported for
 * {@code RedisDocumentRepository#saveAll()} also affects
 * {@code RedisEnhancedRepository#saveAll()} (i.e. {@code @RedisHash}
 * entities), since {@code SimpleRedisEnhancedRepository} pipelines through
 * the same unguarded {@code RedisModulesClient#getJedis().get()} call.
 * <p>
 * This asserts the expected, correct behavior, so it fails on unfixed code
 * and should pass once {@code saveAll()} no longer depends on
 * {@code getJedis()} for pipelining.
 *
 * @see Issue769DocumentSaveAllUnifiedJedisTest
 */
class Issue769EnhancedSaveAllUnifiedJedisTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  CountryRepository countryRepository;

  @MockitoSpyBean
  RedisModulesClient client;

  @AfterEach
  void cleanUp() {
    countryRepository.deleteAll();
  }

  @Test
  void testSaveAllSucceedsWhenNativeConnectionIsNotRawJedis() {
    given(client.getJedis()).willReturn(Optional.empty());

    Country country = Country.of("issue-769-hash");

    List<Country> saved = countryRepository.saveAll(List.of(country));

    assertThat(saved).hasSize(1);
    assertThat(countryRepository.findById(country.getId())).isPresent();
  }
}
