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
import com.redis.om.spring.fixtures.document.model.DocWithRedisKey;
import com.redis.om.spring.fixtures.document.repository.DocWithRedisKeyRepository;

class RedisKeyTest extends AbstractBaseDocumentTest {
  
  @Autowired
  DocWithRedisKeyRepository repository;

  @BeforeEach
  void setup() {
    repository.deleteAll();
  }

  @Test
  void testRedisKeyPopulatedOnFindById() {
    // Given
    DocWithRedisKey doc = new DocWithRedisKey();
    doc.setId("test-id-1");
    doc.setName("Test Document");
    doc.setDescription("Testing @RedisKey functionality");
    repository.save(doc);

    // When
    Optional<DocWithRedisKey> found = repository.findById("test-id-1");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getRedisKey()).isNotNull();
    assertThat(found.get().getRedisKey()).isEqualTo("com.redis.om.spring.fixtures.document.model.DocWithRedisKey:test-id-1");
    assertThat(found.get().getName()).isEqualTo("Test Document");
  }

  @Test
  void testRedisKeyPopulatedOnFindAll() {
    // Given
    DocWithRedisKey doc1 = new DocWithRedisKey();
    doc1.setId("test-id-1");
    doc1.setName("Document 1");
    repository.save(doc1);

    DocWithRedisKey doc2 = new DocWithRedisKey();
    doc2.setId("test-id-2");
    doc2.setName("Document 2");
    repository.save(doc2);

    // When
    List<DocWithRedisKey> all = repository.findAll();

    // Then
    assertThat(all).hasSize(2);
    assertThat(all).allSatisfy(doc -> {
      assertThat(doc.getRedisKey()).isNotNull();
      assertThat(doc.getRedisKey()).startsWith("com.redis.om.spring.fixtures.document.model.DocWithRedisKey:");
    });
  }

  @Test
  void testRedisKeyPopulatedOnCustomQuery() {
    // Given
    DocWithRedisKey doc = new DocWithRedisKey();
    doc.setId("test-id-1");
    doc.setName("Searchable Document");
    doc.setDescription("This should be searchable");
    repository.save(doc);

    // When
    List<DocWithRedisKey> found = repository.findByName("Searchable Document");

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getRedisKey()).isNotNull();
    assertThat(found.get(0).getRedisKey()).isEqualTo("com.redis.om.spring.fixtures.document.model.DocWithRedisKey:test-id-1");
  }

  @Test
  void testRedisKeyPopulatedOnPageQuery() {
    // Given
    for (int i = 0; i < 5; i++) {
      DocWithRedisKey doc = new DocWithRedisKey();
      doc.setId("test-id-" + i);
      doc.setName("Document " + i);
      repository.save(doc);
    }

    // When
    Pageable pageable = PageRequest.of(0, 3);
    Page<DocWithRedisKey> page = repository.findAll(pageable);

    // Then
    assertThat(page.getContent()).hasSize(3);
    assertThat(page.getContent()).allSatisfy(doc -> {
      assertThat(doc.getRedisKey()).isNotNull();
      assertThat(doc.getRedisKey()).startsWith("com.redis.om.spring.fixtures.document.model.DocWithRedisKey:");
    });
  }

  @Test
  void testRedisKeyNotOverwrittenOnSave() {
    // Given
    DocWithRedisKey doc = new DocWithRedisKey();
    doc.setId("test-id-1");
    doc.setName("Test Document");
    doc.setRedisKey("should-be-overwritten");
    repository.save(doc);

    // When
    Optional<DocWithRedisKey> found = repository.findById("test-id-1");

    // Then
    assertThat(found).isPresent();
    // The RedisKey should be populated with the actual Redis key, not the one we set
    assertThat(found.get().getRedisKey()).isEqualTo("com.redis.om.spring.fixtures.document.model.DocWithRedisKey:test-id-1");
  }
}