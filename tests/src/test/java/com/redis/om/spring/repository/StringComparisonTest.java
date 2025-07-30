package com.redis.om.spring.repository;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.StringComparisonTestDoc;
import com.redis.om.spring.fixtures.document.model.StringComparisonTestDoc$;
import com.redis.om.spring.fixtures.document.repository.StringComparisonTestDocRepository;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.search.stream.EntityStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests to demonstrate the missing feature for string comparison operations
 * as described in issue #526: https://github.com/redis/redis-om-spring/issues/526
 *
 * The issue requests support for TextTagField and TextField GreaterThan, LessThan
 * or CompareTo operations for string fields in Redis OM Spring.
 */
class StringComparisonTest extends AbstractBaseDocumentTest {

  @Autowired
  StringComparisonTestDocRepository repository;

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @Autowired
  EntityStream entityStream;

  @BeforeEach
  void setup() {
    repository.deleteAll();

    // Create test data with string values that can be compared lexicographically
    repository.saveAll(Arrays.asList(
      StringComparisonTestDoc.of("AAA", "text_aaa", "text_aaa", "regular_001", "100000001"),
      StringComparisonTestDoc.of("BBB", "text_bbb", "text_bbb", "regular_002", "100000002"),
      StringComparisonTestDoc.of("CCC", "text_ccc", "text_ccc", "regular_003", "100000003"),
      StringComparisonTestDoc.of("DDD", "text_ddd", "text_ddd", "regular_004", "100000004"),
      StringComparisonTestDoc.of("EEE", "text_eee", "text_eee", "regular_005", "100000005")
    ));
  }

  @Test
  void testRepositoryMethodFindByIndexedStringFieldGreaterThan() {
    // Test if findByFieldGreaterThan works for @Indexed string fields
    List<StringComparisonTestDoc> results = repository.findByIndexedStringFieldGreaterThan("BBB");

    // Expected behavior: Should return docs with values > "BBB" (i.e., "CCC", "DDD", "EEE")
    // The feature is now properly supported with lexicographic=true
    assertThat(results).hasSize(3);
    assertThat(results.stream().map(StringComparisonTestDoc::getIndexedStringField))
      .containsExactlyInAnyOrder("CCC", "DDD", "EEE");
  }

  @Test
  void testRepositoryMethodFindByComIdGreaterThan() {
    // Test the specific use case from the issue - comparing comId strings
    List<StringComparisonTestDoc> results = repository.findByComIdGreaterThan("100000003");

    // Expected behavior: Should return docs with comId > "100000003" (i.e., "100000004", "100000005")
    // The feature is now properly supported with lexicographic=true
    assertThat(results).hasSize(2);
    assertThat(results.stream().map(StringComparisonTestDoc::getComId))
      .containsExactlyInAnyOrder("100000004", "100000005");
  }

  @Test
  void testEntityStreamWithGtOperationOnStringField() {
    // This test demonstrates that gt() is supported for string fields in EntityStream
    // when the field is marked with lexicographic=true

    // This is what the user wants to do (from issue #526):
    List<StringComparisonTestDoc> results = entityStream
      .of(StringComparisonTestDoc.class)
      .filter(StringComparisonTestDoc$.COM_ID.gt("100000003"))
      .collect(Collectors.toList());

    // The feature is now implemented for fields with lexicographic=true
    assertThat(results).hasSize(2);
    assertThat(results.stream().map(StringComparisonTestDoc::getComId))
      .containsExactlyInAnyOrder("100000004", "100000005");
  }

  @Test
  void testEntityStreamWithLtOperationOnStringField() {
    // This test demonstrates that lt() is supported for string fields in EntityStream
    // when the field is marked with lexicographic=true

    // This is what the user wants to do:
    List<StringComparisonTestDoc> results = entityStream
      .of(StringComparisonTestDoc.class)
      .filter(StringComparisonTestDoc$.COM_ID.lt("100000003"))
      .collect(Collectors.toList());

    assertThat(results).hasSize(2);
    assertThat(results.stream().map(StringComparisonTestDoc::getComId))
      .containsExactlyInAnyOrder("100000001", "100000002");
  }

  @Test
  void testQueryByExampleWithStringComparison() {
    // Test Query By Example with string comparison
    StringComparisonTestDoc probe = new StringComparisonTestDoc();
    probe.setComId("100000003");

    // ExampleMatcher only supports exact, contains, startsWith, endsWith, and regex
    // It does NOT support greater than or less than comparisons
    ExampleMatcher matcher = ExampleMatcher.matching()
      .withMatcher("comId", ExampleMatcher.GenericPropertyMatchers.exact());

    Example<StringComparisonTestDoc> example = Example.of(probe, matcher);
    List<StringComparisonTestDoc> results = (List<StringComparisonTestDoc>) repository.findAll(example);

    // This will only find exact matches, not greater/less than
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getComId()).isEqualTo("100000003");

    // There's no way to create an ExampleMatcher for > or < comparisons
    // Note: ExampleMatcher does not support comparison operations (>, <, >=, <=) for strings
  }


}