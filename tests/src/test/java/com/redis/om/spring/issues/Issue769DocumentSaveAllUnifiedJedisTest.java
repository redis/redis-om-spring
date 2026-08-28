package com.redis.om.spring.issues;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.fixtures.document.model.LexicographicDoc;
import com.redis.om.spring.fixtures.document.model.Movie;
import com.redis.om.spring.fixtures.document.repository.LexicographicDocRepository;
import com.redis.om.spring.fixtures.document.repository.MovieRepository;

/**
 * Regression test for issue #769: {@code saveAll()} throws
 * {@code NoSuchElementException: No value present} whenever
 * {@link RedisModulesClient#getJedis()} cannot unwrap the Spring-managed
 * {@code JedisConnectionFactory}'s native connection into a raw {@code Jedis}
 * instance.
 * <p>
 * In production this happens with Spring Data Redis 4.1+, whose
 * {@code JedisConnectionFactory} defaults {@code useUnifiedJedis} to
 * {@code true} whenever {@code redis.clients.jedis.RedisClient} is on the
 * classpath (true for Jedis 7.2+), making {@code getNativeConnection()}
 * return a {@code UnifiedJedis}-based connection instead of a raw
 * {@code Jedis}. This test reproduces that same failure mode directly,
 * without depending on any specific Spring Data Redis/Jedis version
 * combination, by spying on {@link RedisModulesClient} and forcing
 * {@code getJedis()} to return {@code Optional.empty()}, exactly as it does
 * in the affected environment.
 * <p>
 * This asserts the expected, correct behavior — {@code saveAll()} must
 * succeed regardless of whether {@code getJedis()} can produce a raw
 * {@code Jedis} — so it fails on unfixed code and should pass once
 * {@code saveAll()} no longer depends on {@code getJedis()} for pipelining.
 */
class Issue769DocumentSaveAllUnifiedJedisTest extends AbstractBaseDocumentTest {

  @Autowired
  MovieRepository movieRepository;

  @Autowired
  LexicographicDocRepository lexicographicDocRepository;

  @MockitoSpyBean
  RedisModulesClient client;

  @AfterEach
  void cleanUp() {
    movieRepository.deleteAll();
    lexicographicDocRepository.deleteAll();
  }

  @Test
  void testSaveAllSucceedsWhenNativeConnectionIsNotRawJedis() {
    given(client.getJedis()).willReturn(Optional.empty());

    Movie movie = new Movie();
    movie.setNode("issue-769");

    List<Movie> saved = movieRepository.saveAll(List.of(movie));

    assertThat(saved).hasSize(1);
    assertThat(movieRepository.findById(saved.get(0).getId())).isPresent();
  }

  /**
   * {@code save()} only takes the safe path (delegating to
   * {@code super.save()}, which never calls {@code getJedis()}) for entities
   * with no {@code @Lexicographic} fields. Entities that do have one, such as
   * {@link LexicographicDoc}, make {@code save()} delegate to
   * {@code saveAll()} internally, so {@code save()} is just as exposed to
   * this bug as {@code saveAll()} is.
   */
  @Test
  void testSaveSucceedsWhenNativeConnectionIsNotRawJedisAndEntityHasLexicographicFields() {
    given(client.getJedis()).willReturn(Optional.empty());

    LexicographicDoc doc = LexicographicDoc.of("sku-769", "issue-769", "category-769", "active");

    LexicographicDoc saved = lexicographicDocRepository.save(doc);

    assertThat(saved).isNotNull();
    assertThat(lexicographicDocRepository.findById(saved.getId())).isPresent();
  }
}
