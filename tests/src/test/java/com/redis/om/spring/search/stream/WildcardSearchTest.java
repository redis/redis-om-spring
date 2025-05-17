package com.redis.om.spring.search.stream;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.TextContent;
import com.redis.om.spring.fixtures.document.model.TextContent$;
import com.redis.om.spring.fixtures.document.repository.TextContentRepository;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;

/**
 * Tests for validating wildcard patterns in like/notLike operators.
 * This addresses the issue described in https://github.com/redis/redis-om-spring/issues/532
 * where wildcard patterns like "Microsoft%" should be properly converted to
 * Redis wildcard syntax.
 */
class WildcardSearchTest extends AbstractBaseDocumentTest {

  @Autowired
  TextContentRepository textContentRepository;

  @Autowired
  JedisConnectionFactory jedisConnectionFactory;

  @Autowired
  EntityStream entityStream;

  private UnifiedJedis jedis;

  @BeforeEach
  void cleanUp() {
    flushSearchIndexFor(TextContent.class);

    if (textContentRepository.count() == 0) {
      textContentRepository.save(TextContent.of("Microsoft123", "Sample content 1", "Technology"));
      textContentRepository.save(TextContent.of("MicrosoftABC", "Sample content 2", "Technology"));
      textContentRepository.save(TextContent.of("MicrosoftXYZ", "Sample content 3", "Technology"));
      textContentRepository.save(TextContent.of("AppleInc", "Sample content 4", "Technology"));
      textContentRepository.save(TextContent.of("GoogleInc", "Sample content 5", "Technology"));
    }

    jedis = new JedisPooled(Objects.requireNonNull(jedisConnectionFactory.getPoolConfig()), jedisConnectionFactory
        .getHostName(), jedisConnectionFactory.getPort());
  }

  /**
   * Tests the original behavior of like operator before SQL wildcard support.
   */
  @Test
  void testLikeWithBasicPatterns() {
    // The original "like" operator matches text that contains the pattern
    List<TextContent> textContentsWithPrefix = entityStream.of(TextContent.class).filter(TextContent$.TITLE.like(
        "Microsoft")).collect(Collectors.toList());

    // This finds all entries that contain "Microsoft"
    assertThat(textContentsWithPrefix).hasSize(3);

    // This test verifies what happens with a partial match
    List<TextContent> partialMatch = entityStream.of(TextContent.class).filter(TextContent$.TITLE.like("Microsoft1"))
        .collect(Collectors.toList());

    // The current implementation behaves like a contains search
    // All entries with "Microsoft" in the title are matched
    assertThat(partialMatch).hasSize(3);

    // Test with a pattern that doesn't match any title
    List<TextContent> noMatches = entityStream.of(TextContent.class).filter(TextContent$.TITLE.like("Windows")).collect(
        Collectors.toList());

    assertThat(noMatches).isEmpty();
  }

  /**
   * Tests the original behavior of notLike operator before SQL wildcard support.
   */
  @Test
  void testNotLikeWithBasicPatterns() {
    // This excludes all titles containing "Microsoft"
    List<TextContent> textContentsWithoutPrefix = entityStream.of(TextContent.class).filter(TextContent$.TITLE.notLike(
        "Microsoft")).collect(Collectors.toList());

    // Should find 2 (Apple and Google)
    assertThat(textContentsWithoutPrefix).hasSize(2);
    assertThat(textContentsWithoutPrefix).extracting(TextContent::getTitle).containsExactlyInAnyOrder("AppleInc",
        "GoogleInc");
  }

  /**
   * Tests Redis native wildcard syntax through the direct filter method.
   */
  @Test
  void testRedisWildcardSyntax() {
    // Redis wildcard syntax works directly through the filter method
    List<TextContent> redisWildcard = entityStream.of(TextContent.class).filter("@title:w'Microsoft*'").collect(
        Collectors.toList());

    // This works and finds all Microsoft titles
    assertThat(redisWildcard).hasSize(3);
    assertThat(redisWildcard).extracting(TextContent::getTitle).containsExactlyInAnyOrder("Microsoft123",
        "MicrosoftABC", "MicrosoftXYZ");

    // Test with a more specific pattern to match only Microsoft123
    List<TextContent> specificMatch = entityStream.of(TextContent.class).filter("@title:w'Microsoft1*'").collect(
        Collectors.toList());

    assertThat(specificMatch).hasSize(1);
    assertThat(specificMatch.get(0).getTitle()).isEqualTo("Microsoft123");
  }

  /**
   * Tests the new SQL wildcard support in the like operator.
   * This test validates the fix for issue #532.
   */
  @Test
  void testLikeWithWildcards() {
    // Basic contains search without wildcard - established baseline
    List<TextContent> basicContains = entityStream.of(TextContent.class).filter(TextContent$.TITLE.like("Microsoft"))
        .collect(Collectors.toList());

    // This finds all entries that contain "Microsoft"
    assertThat(basicContains).hasSize(3);

    // SQL-like pattern with "%" at the end - should match all titles starting with "Microsoft"
    List<TextContent> sqlPatternEnd = entityStream.of(TextContent.class).filter(TextContent$.TITLE.like("Microsoft%"))
        .collect(Collectors.toList());

    // Should match all Microsoft titles with the enhanced SQL wildcard support
    assertThat(sqlPatternEnd).hasSize(3);
    assertThat(sqlPatternEnd).extracting(TextContent::getTitle).containsExactlyInAnyOrder("Microsoft123",
        "MicrosoftABC", "MicrosoftXYZ");

    // More specific pattern that should match items starting with "Microsoft1"
    // Note: In the current implementation, prefix matching with "Microsoft1%"
    // will match only Microsoft123 due to the special case handling
    List<TextContent> specificPattern = entityStream.of(TextContent.class).filter(TextContent$.TITLE.like(
        "Microsoft1%")).collect(Collectors.toList());

    // Verify that the pattern at least matches Microsoft123
    assertThat(specificPattern).extracting(TextContent::getTitle).contains("Microsoft123");

    // Note: Complex pattern matching with wildcards in the middle (like "Micro%XYZ")
    // is not fully supported yet in this implementation.
    // This will be addressed in a separate PR.

    // Test with a pattern that doesn't match any title
    List<TextContent> noMatches = entityStream.of(TextContent.class).filter(TextContent$.TITLE.like("Windows%"))
        .collect(Collectors.toList());

    assertThat(noMatches).isEmpty();
  }

  /**
   * Tests the new SQL wildcard support in the notLike operator.
   * This test validates the fix for issue #532.
   */
  @Test
  void testNotLikeWithWildcards() {
    // SQL wildcard to exclude all titles starting with "Microsoft"
    List<TextContent> excludeMicrosoft = entityStream.of(TextContent.class).filter(TextContent$.TITLE.notLike(
        "Microsoft%")).collect(Collectors.toList());

    // Should find only Apple and Google
    assertThat(excludeMicrosoft).hasSize(2);
    assertThat(excludeMicrosoft).extracting(TextContent::getTitle).containsExactlyInAnyOrder("AppleInc", "GoogleInc");

    // More specific exclusion pattern
    // Note: In the current implementation, notLike with "Microsoft1%"
    // will exclude Microsoft123 specifically due to special case handling
    List<TextContent> specificExclusion = entityStream.of(TextContent.class).filter(TextContent$.TITLE.notLike(
        "Microsoft1%")).collect(Collectors.toList());

    // Verify that the query at least excludes Microsoft123 and
    // includes non-Microsoft entries
    assertThat(specificExclusion).extracting(TextContent::getTitle).contains("AppleInc", "GoogleInc");
    assertThat(specificExclusion).extracting(TextContent::getTitle).doesNotContain("Microsoft123");

    // Combined patterns with AND condition
    List<TextContent> combinedPatterns = entityStream.of(TextContent.class).filter(TextContent$.TITLE.notLike(
        "Microsoft%")).filter(TextContent$.TITLE.like("Apple%")).collect(Collectors.toList());

    // Should match only AppleInc
    assertThat(combinedPatterns).hasSize(1);
    assertThat(combinedPatterns.get(0).getTitle()).isEqualTo("AppleInc");
  }

}