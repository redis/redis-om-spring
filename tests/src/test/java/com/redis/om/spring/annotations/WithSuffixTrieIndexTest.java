package com.redis.om.spring.annotations;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import com.redis.om.spring.AbstractBaseOMTest;
import com.redis.om.spring.TestConfig;
import com.redis.om.spring.fixtures.document.model.WithSuffixTrieAutodetectedNestedDocument;

import lombok.Data;

@DirtiesContext
@SpringBootTest(
    classes = WithSuffixTrieIndexTest.Config.class,
    properties = { "spring.main.allow-bean-definition-overriding=true" }
)
@TestPropertySource(
    properties = { "spring.config.location=classpath:vss_on.yaml" }
)
class WithSuffixTrieIndexTest extends AbstractBaseOMTest {
  private static final String DOCUMENT_INDEX = "with_suffix_trie_document_idx";
  private static final String HASH_INDEX = "with_suffix_trie_hash_idx";
  private static final String NESTED_DOCUMENT_INDEX = "with_suffix_trie_nested_document_idx";
  private static final String AUTODETECTED_NESTED_DOCUMENT_INDEX = "with_suffix_trie_autodetected_nested_document_idx";

  @AfterEach
  void tearDown() {
    dropIndex(DOCUMENT_INDEX);
    dropIndex(HASH_INDEX);
    dropIndex(NESTED_DOCUMENT_INDEX);
    dropIndex(AUTODETECTED_NESTED_DOCUMENT_INDEX);
  }

  @Test
  void appliesWithSuffixTrieToDocumentTextAndTagFields() {
    assertThat(indexer.createIndexFor(WithSuffixTrieDocument.class, DOCUMENT_INDEX, "with-suffix-trie-doc:")).isTrue();

    assertFieldsHaveSuffixTrie(DOCUMENT_INDEX, "searchable", "text", "tag", "indexed", "explicitTag");
  }

  @Test
  void appliesWithSuffixTrieToHashTextAndTagFields() {
    assertThat(indexer.createIndexFor(WithSuffixTrieHash.class, HASH_INDEX, "with-suffix-trie-hash:")).isTrue();

    assertFieldsHaveSuffixTrie(HASH_INDEX, "searchable", "text", "tag", "indexed", "explicitTag");
  }

  @Test
  void appliesWithSuffixTrieToNestedDocumentTextFields() {
    assertThat(indexer.createIndexFor(WithSuffixTrieNestedDocument.class, NESTED_DOCUMENT_INDEX,
        "with-suffix-trie-nested-doc:")).isTrue();

    assertFieldsHaveSuffixTrie(NESTED_DOCUMENT_INDEX, "items_searchable", "items_text");
    assertFieldsHaveType(NESTED_DOCUMENT_INDEX, "TEXT", "items_searchable", "items_text");
  }

  @Test
  void appliesWithSuffixTrieToAutodetectedNestedDocumentTextFields() {
    assertThat(indexer.createIndexFor(WithSuffixTrieAutodetectedNestedDocument.class,
        AUTODETECTED_NESTED_DOCUMENT_INDEX, "with-suffix-trie-autodetected-nested-doc:")).isTrue();

    assertFieldsHaveSuffixTrie(AUTODETECTED_NESTED_DOCUMENT_INDEX, "autodetectedItems_searchable",
        "autodetectedItems_text");
    assertFieldsHaveType(AUTODETECTED_NESTED_DOCUMENT_INDEX, "TEXT", "autodetectedItems_searchable",
        "autodetectedItems_text");
  }

  private void assertFieldsHaveSuffixTrie(String indexName, String... fieldNames) {
    List<List<String>> attributes = attributesFor(indexName);

    for (String fieldName : fieldNames) {
      assertThat(attributeFor(attributes, fieldName)).contains("WITHSUFFIXTRIE");
    }
  }

  private void assertFieldsHaveType(String indexName, String type, String... fieldNames) {
    List<List<String>> attributes = attributesFor(indexName);

    for (String fieldName : fieldNames) {
      assertThat(valueAfter(attributeFor(attributes, fieldName), "type")).isEqualTo(type);
    }
  }

  private List<List<String>> attributesFor(String indexName) {
    Map<String, Object> info = modulesOperations.opsForSearch(indexName).getInfo();
    Object rawAttributes = info.get("attributes");

    assertThat(rawAttributes).isInstanceOf(List.class);
    return ((List<?>) rawAttributes).stream().map(attribute -> {
      assertThat(attribute).isInstanceOf(List.class);
      return ((List<?>) attribute).stream().map(String::valueOf).toList();
    }).toList();
  }

  private List<String> attributeFor(List<List<String>> attributes, String fieldName) {
    return attributes.stream().filter(attribute -> fieldName.equals(valueAfter(attribute, "attribute"))).findFirst()
        .orElseThrow(() -> new AssertionError("No Redis Search attribute found for field " + fieldName));
  }

  private String valueAfter(List<String> values, String key) {
    for (int i = 0; i < values.size() - 1; i++) {
      if (key.equals(values.get(i))) {
        return values.get(i + 1);
      }
    }
    return null;
  }

  private void dropIndex(String indexName) {
    try {
      modulesOperations.opsForSearch(indexName).dropIndex();
    } catch (Exception ignored) {
    }
  }

  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(
      basePackageClasses = WithSuffixTrieIndexTest.class
  )
  @EnableRedisEnhancedRepositories(
      basePackageClasses = WithSuffixTrieIndexTest.class
  )
  static class Config extends TestConfig {
  }

  @Data
  @Document
  static class WithSuffixTrieDocument {
    @Id
    private String id;

    @Searchable(
        withSuffixTrie = true
    )
    private String searchable;

    @TextIndexed(
        withSuffixTrie = true
    )
    private String text;

    @TagIndexed(
        withSuffixTrie = true
    )
    private String tag;

    @Indexed(
        withSuffixTrie = true
    )
    private String indexed;

    @Indexed(
        schemaFieldType = SchemaFieldType.TAG,
        withSuffixTrie = true
    )
    private String explicitTag;
  }

  @Data
  @Document
  static class WithSuffixTrieNestedDocument {
    @Id
    private String id;

    @Indexed(
        schemaFieldType = SchemaFieldType.NESTED
    )
    private List<WithSuffixTrieNestedItem> items;
  }

  @Data
  static class WithSuffixTrieNestedItem {
    @Searchable(
        withSuffixTrie = true
    )
    private String searchable;

    @TextIndexed(
        withSuffixTrie = true
    )
    private String text;
  }

  @Data
  @RedisHash(
    "with-suffix-trie-hash"
  )
  static class WithSuffixTrieHash {
    @Id
    private String id;

    @Searchable(
        withSuffixTrie = true
    )
    private String searchable;

    @TextIndexed(
        withSuffixTrie = true
    )
    private String text;

    @TagIndexed(
        withSuffixTrie = true
    )
    private String tag;

    @Indexed(
        withSuffixTrie = true
    )
    private String indexed;

    @Indexed(
        schemaFieldType = SchemaFieldType.TAG,
        withSuffixTrie = true
    )
    private String explicitTag;
  }
}
