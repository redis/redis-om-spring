package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import com.redis.om.spring.AbstractBaseOMTest;
import com.redis.om.spring.TestConfig;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.fixtures.document.model.PhoneticChunkDoc;
import com.redis.om.spring.fixtures.document.model.PhoneticDoc;
import com.redis.om.spring.fixtures.document.model.PhoneticMultiFieldDoc;
import com.redis.om.spring.fixtures.document.model.PhoneticReferenceDoc;
import com.redis.om.spring.fixtures.document.model.PhoneticTextIndexedDoc;
import com.redis.om.spring.fixtures.hash.model.PhoneticHash;
import com.redis.om.spring.ops.search.SearchOperations;

import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.schemafields.SchemaField;
import redis.clients.jedis.search.schemafields.TagField;
import redis.clients.jedis.search.schemafields.TextField;

@DirtiesContext
@SpringBootTest(
    classes = SearchablePhoneticTest.Config.class,
    properties = { "spring.main.allow-bean-definition-overriding=true" }
)
class SearchablePhoneticTest extends AbstractBaseOMTest {
  private static final String INDEX_NAME = "searchable_phonetic_test_idx";
  private static final String PREFIX = "searchable_phonetic_test:";
  private static final String KEY = PREFIX + "1";
  private static final String CHUNK_INDEX_NAME = "searchable_phonetic_chunk_test_idx";
  private static final String CHUNK_PREFIX = "searchable_phonetic_chunk_test:";
  private static final String CHUNK_KEY = CHUNK_PREFIX + "1";
  private static final String TEXT_INDEXED_INDEX_NAME = "searchable_phonetic_text_indexed_test_idx";
  private static final String TEXT_INDEXED_PREFIX = "searchable_phonetic_text_indexed_test:";
  private static final String TEXT_INDEXED_KEY = TEXT_INDEXED_PREFIX + "1";
  private static final String MULTI_FIELD_INDEX_NAME = "searchable_phonetic_multi_field_test_idx";
  private static final String MULTI_FIELD_PREFIX = "searchable_phonetic_multi_field_test:";
  private static final String MULTI_FIELD_KEY = MULTI_FIELD_PREFIX + "1";
  private static final String REFERENCE_INDEX_NAME = "searchable_phonetic_reference_test_idx";
  private static final String REFERENCE_PREFIX = "searchable_phonetic_reference_test:";
  private static final String REFERENCE_KEY = REFERENCE_PREFIX + "1";
  private static final String HASH_INDEX_NAME = "searchable_phonetic_hash_test_idx";
  private static final String HASH_PREFIX = "searchable_phonetic_hash_test:";
  private static final String HASH_KEY = HASH_PREFIX + "1";
  private static final String TEXT_FIRST_INDEX_NAME = "searchable_phonetic_text_first_test_idx";
  private static final String TEXT_FIRST_PREFIX = "searchable_phonetic_text_first_test:";
  private static final String TEXT_FIRST_KEY = TEXT_FIRST_PREFIX + "1";
  private static final String TAG_FIRST_INDEX_NAME = "searchable_phonetic_tag_first_test_idx";
  private static final String TAG_FIRST_PREFIX = "searchable_phonetic_tag_first_test:";
  private static final String TAG_FIRST_KEY = TAG_FIRST_PREFIX + "1";

  @BeforeEach
  void setUp() {
    cleanRedis();
  }

  @AfterEach
  void cleanUp() {
    cleanRedis();
  }

  @Test
  void searchablePhoneticCreatesRedisPhoneticSchemaAndMatchesSoundAlikeTerms() {
    assertTrue(indexer.createIndexFor(PhoneticDoc.class, INDEX_NAME, PREFIX));

    modulesOperations.opsForJSON().set(KEY, Map.of("phoneticName", "John", "plainName", "John"));

    SearchOperations<String> ops = modulesOperations.opsForSearch(INDEX_NAME);
    SearchResult phoneticResult = ops.search(new Query("@phoneticName:Jon"));
    SearchResult plainResult = ops.search(new Query("@plainName:Jon"));

    assertThat(phoneticResult.getTotalResults()).isEqualTo(1);
    assertThat(plainResult.getTotalResults()).isZero();
  }

  @Test
  void searchablePhoneticFieldAfterIndexedTagFieldMatchesSoundAlikeTerms() {
    assertTrue(indexer.createIndexFor(PhoneticChunkDoc.class, CHUNK_INDEX_NAME, CHUNK_PREFIX));

    modulesOperations.opsForJSON().set(CHUNK_KEY, Map.of("chunkText", "cash", "filingDate", "2026-06-15"));

    SearchResult result = modulesOperations.opsForSearch(CHUNK_INDEX_NAME).search(new Query("@chunkText:kash"));

    assertThat(result.getTotalResults()).isEqualTo(1);
  }

  @Test
  void textIndexedPhoneticFieldAfterIndexedTagFieldMatchesSoundAlikeTerms() {
    assertTrue(indexer.createIndexFor(PhoneticTextIndexedDoc.class, TEXT_INDEXED_INDEX_NAME, TEXT_INDEXED_PREFIX));

    modulesOperations.opsForJSON().set(TEXT_INDEXED_KEY, Map.of("chunkText", "cash", "filingDate", "2026-06-15"));

    SearchResult result = modulesOperations.opsForSearch(TEXT_INDEXED_INDEX_NAME).search(new Query("@chunkText:kash"));

    assertThat(result.getTotalResults()).isEqualTo(1);
  }

  @Test
  void multipleSearchablePhoneticFieldsAfterIndexedTagFieldsMatchSoundAlikeTerms() {
    assertTrue(indexer.createIndexFor(PhoneticMultiFieldDoc.class, MULTI_FIELD_INDEX_NAME, MULTI_FIELD_PREFIX));

    modulesOperations.opsForJSON().set(MULTI_FIELD_KEY, Map.of(
        "chunkText", "cash",
        "customerName", "John",
        "filingDate", "2026-06-15",
        "category", "contract"
    ));

    SearchOperations<String> ops = modulesOperations.opsForSearch(MULTI_FIELD_INDEX_NAME);

    assertThat(ops.search(new Query("@chunkText:kash")).getTotalResults()).isEqualTo(1);
    assertThat(ops.search(new Query("@customerName:Jon")).getTotalResults()).isEqualTo(1);
  }

  @Test
  void referencedSearchablePhoneticFieldIsCreatedBeforeReferenceTagField() {
    assertTrue(indexer.createIndexFor(PhoneticReferenceDoc.class, REFERENCE_INDEX_NAME, REFERENCE_PREFIX));

    List<String> attributes = attributeNames(REFERENCE_INDEX_NAME);

    assertThat(attributes).contains("owner_name", "owner");
    assertThat(attributes.indexOf("owner_name")).isLessThan(attributes.indexOf("owner"));
  }

  @Test
  void searchablePhoneticHashFieldAfterIndexedTagFieldMatchesSoundAlikeTerms() {
    assertTrue(indexer.createIndexFor(PhoneticHash.class, HASH_INDEX_NAME, HASH_PREFIX));

    template.opsForHash().putAll(HASH_KEY, Map.of("chunkText", "cash", "filingDate", "2026-06-15"));

    SearchResult result = modulesOperations.opsForSearch(HASH_INDEX_NAME).search(new Query("@chunkText:kash"));

    assertThat(result.getTotalResults()).isEqualTo(1);
  }

  @Test
  void textPhoneticFieldBeforeTagFieldMatchesSoundAlikeTerms() {
    createIndex(TEXT_FIRST_INDEX_NAME, TEXT_FIRST_PREFIX, List.of(
        TextField.of(FieldName.of("$.chunkText").as("chunkText")).phonetic("dm:en"),
        TagField.of(FieldName.of("$.filingDate").as("filingDate"))
    ));
    modulesOperations.opsForJSON().set(TEXT_FIRST_KEY, Map.of("chunkText", "cash", "filingDate", "2026-06-15"));

    SearchResult result = modulesOperations.opsForSearch(TEXT_FIRST_INDEX_NAME).search(new Query("@chunkText:kash"));

    assertThat(result.getTotalResults()).isEqualTo(1);
  }

  @Test
  void tagFieldBeforeTextPhoneticFieldDoesNotMatchSoundAlikeTerms() {
    createIndex(TAG_FIRST_INDEX_NAME, TAG_FIRST_PREFIX, List.of(
        TagField.of(FieldName.of("$.filingDate").as("filingDate")),
        TextField.of(FieldName.of("$.chunkText").as("chunkText")).phonetic("dm:en")
    ));
    modulesOperations.opsForJSON().set(TAG_FIRST_KEY, Map.of("chunkText", "cash", "filingDate", "2026-06-15"));

    SearchResult result = modulesOperations.opsForSearch(TAG_FIRST_INDEX_NAME).search(new Query("@chunkText:kash"));

    assertThat(result.getTotalResults()).isZero();
  }

  private void createIndex(String indexName, String prefix, List<SchemaField> fields) {
    FTCreateParams params = FTCreateParams.createParams().on(IndexDataType.JSON).prefix(prefix);
    modulesOperations.opsForSearch(indexName).createIndex(params, fields);
  }

  private List<String> attributeNames(String indexName) {
    Object rawAttributes = modulesOperations.opsForSearch(indexName).getInfo().get("attributes");
    assertThat(rawAttributes).isInstanceOf(List.class);

    List<String> names = new ArrayList<>();
    for (Object rawAttribute : (List<?>) rawAttributes) {
      assertThat(rawAttribute).isInstanceOf(List.class);
      List<?> attribute = (List<?>) rawAttribute;
      int attributeNameIndex = attribute.indexOf("attribute");
      assertThat(attributeNameIndex).isGreaterThanOrEqualTo(0);
      names.add(attribute.get(attributeNameIndex + 1).toString());
    }
    return names;
  }

  private void cleanRedis() {
    dropIndex(INDEX_NAME);
    dropIndex(CHUNK_INDEX_NAME);
    dropIndex(TEXT_INDEXED_INDEX_NAME);
    dropIndex(MULTI_FIELD_INDEX_NAME);
    dropIndex(REFERENCE_INDEX_NAME);
    dropIndex(HASH_INDEX_NAME);
    dropIndex(TEXT_FIRST_INDEX_NAME);
    dropIndex(TAG_FIRST_INDEX_NAME);
    template.delete(KEY);
    template.delete(CHUNK_KEY);
    template.delete(TEXT_INDEXED_KEY);
    template.delete(MULTI_FIELD_KEY);
    template.delete(REFERENCE_KEY);
    template.delete(HASH_KEY);
    template.delete(TEXT_FIRST_KEY);
    template.delete(TAG_FIRST_KEY);
  }

  private void dropIndex(String indexName) {
    try {
      modulesOperations.opsForSearch(indexName).dropIndexAndDocuments();
    } catch (JedisDataException e) {
      String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
      if (!message.contains("unknown index") && !message.contains("unknown index name") && !message.contains(
          "no such index")) {
        throw e;
      }
    }
  }

  @SpringBootApplication
  @Configuration
  @EnableRedisDocumentRepositories(
      basePackageClasses = SearchablePhoneticTest.class
  )
  static class Config extends TestConfig {
  }
}
