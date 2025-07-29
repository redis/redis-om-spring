package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.DocWithCustomPrefixAndRedisKey;
import com.redis.om.spring.fixtures.document.repository.DocWithCustomPrefixAndRedisKeyRepository;

class RedisKeyCustomPrefixTest extends AbstractBaseDocumentTest {
  
  @Autowired
  DocWithCustomPrefixAndRedisKeyRepository repository;

  @BeforeEach
  void setup() {
    repository.deleteAll();
  }

  @Test
  void testRedisKeyWithCustomPrefixOnFindById() {
    // Given
    DocWithCustomPrefixAndRedisKey doc = new DocWithCustomPrefixAndRedisKey();
    doc.setId("test-id-1");
    doc.setName("Test Document");
    doc.setDescription("Testing @RedisKey with custom prefix");
    repository.save(doc);

    // When
    Optional<DocWithCustomPrefixAndRedisKey> found = repository.findById("test-id-1");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getRedisKey()).isNotNull();
    assertThat(found.get().getRedisKey()).isEqualTo("custom:doc:test-id-1");
    assertThat(found.get().getName()).isEqualTo("Test Document");
  }

  @Test
  void testRedisKeyWithCustomPrefixOnFindAll() {
    // Given
    DocWithCustomPrefixAndRedisKey doc1 = new DocWithCustomPrefixAndRedisKey();
    doc1.setId("test-id-1");
    doc1.setName("Document 1");
    repository.save(doc1);

    DocWithCustomPrefixAndRedisKey doc2 = new DocWithCustomPrefixAndRedisKey();
    doc2.setId("test-id-2");
    doc2.setName("Document 2");
    repository.save(doc2);

    // When
    List<DocWithCustomPrefixAndRedisKey> all = repository.findAll();

    // Then
    assertThat(all).hasSize(2);
    assertThat(all).allSatisfy(doc -> {
      assertThat(doc.getRedisKey()).isNotNull();
      assertThat(doc.getRedisKey()).startsWith("custom:doc:");
    });
  }

  @Test
  void testRedisKeyWithCustomPrefixOnCustomQuery() {
    // Given
    DocWithCustomPrefixAndRedisKey doc = new DocWithCustomPrefixAndRedisKey();
    doc.setId("test-id-1");
    doc.setName("Searchable Document");
    doc.setDescription("This should be searchable");
    repository.save(doc);

    // When
    List<DocWithCustomPrefixAndRedisKey> found = repository.findByName("Searchable Document");

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getRedisKey()).isNotNull();
    assertThat(found.get(0).getRedisKey()).isEqualTo("custom:doc:test-id-1");
  }

  @Test
  void testRedisKeyWithCustomPrefixOnPageQuery() {
    // Given
    for (int i = 0; i < 5; i++) {
      DocWithCustomPrefixAndRedisKey doc = new DocWithCustomPrefixAndRedisKey();
      doc.setId("test-id-" + i);
      doc.setName("Document " + i);
      repository.save(doc);
    }

    // When
    Pageable pageable = PageRequest.of(0, 3);
    Page<DocWithCustomPrefixAndRedisKey> page = repository.findAll(pageable);

    // Then
    assertThat(page.getContent()).hasSize(3);
    assertThat(page.getContent()).allSatisfy(doc -> {
      assertThat(doc.getRedisKey()).isNotNull();
      assertThat(doc.getRedisKey()).startsWith("custom:doc:");
    });
  }

  @Test
  void testRedisKeyWithCustomPrefixNotOverwrittenOnSave() {
    // Given
    DocWithCustomPrefixAndRedisKey doc = new DocWithCustomPrefixAndRedisKey();
    doc.setId("test-id-1");
    doc.setName("Test Document");
    doc.setRedisKey("should-be-overwritten");
    repository.save(doc);

    // When
    Optional<DocWithCustomPrefixAndRedisKey> found = repository.findById("test-id-1");

    // Then
    assertThat(found).isPresent();
    // The RedisKey should be populated with the actual Redis key, not the one we set
    assertThat(found.get().getRedisKey()).isEqualTo("custom:doc:test-id-1");
  }
}