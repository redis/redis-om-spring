package com.redis.om.spring.annotations.hash;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.HashWithCustomPrefixAndRedisKey;
import com.redis.om.spring.fixtures.hash.repository.HashWithCustomPrefixAndRedisKeyRepository;

class RedisKeyCustomPrefixHashTest extends AbstractBaseEnhancedRedisTest {
  
  @Autowired
  HashWithCustomPrefixAndRedisKeyRepository repository;

  @BeforeEach
  void setup() {
    repository.deleteAll();
  }

  @Test
  void testRedisKeyWithCustomPrefixOnFindById() {
    // Given
    HashWithCustomPrefixAndRedisKey hash = new HashWithCustomPrefixAndRedisKey();
    hash.setId("test-id-1");
    hash.setName("Test Hash");
    hash.setDescription("Testing @RedisKey with custom prefix");
    repository.save(hash);

    // When
    Optional<HashWithCustomPrefixAndRedisKey> found = repository.findById("test-id-1");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getRedisKey()).isNotNull();
    assertThat(found.get().getRedisKey()).isEqualTo("custom:hash:test-id-1");
    assertThat(found.get().getName()).isEqualTo("Test Hash");
  }

  @Test
  void testRedisKeyWithCustomPrefixOnFindAll() {
    // Given
    HashWithCustomPrefixAndRedisKey hash1 = new HashWithCustomPrefixAndRedisKey();
    hash1.setId("test-id-1");
    hash1.setName("Hash 1");
    repository.save(hash1);

    HashWithCustomPrefixAndRedisKey hash2 = new HashWithCustomPrefixAndRedisKey();
    hash2.setId("test-id-2");
    hash2.setName("Hash 2");
    repository.save(hash2);

    // When
    List<HashWithCustomPrefixAndRedisKey> all = repository.findAll();

    // Then
    assertThat(all).hasSize(2);
    assertThat(all).allSatisfy(hash -> {
      assertThat(hash.getRedisKey()).isNotNull();
      assertThat(hash.getRedisKey()).startsWith("custom:hash:");
    });
  }

  @Test
  void testRedisKeyWithCustomPrefixOnCustomQuery() {
    // Given
    HashWithCustomPrefixAndRedisKey hash = new HashWithCustomPrefixAndRedisKey();
    hash.setId("test-id-1");
    hash.setName("Searchable Hash");
    hash.setDescription("This should be searchable");
    repository.save(hash);

    // When
    List<HashWithCustomPrefixAndRedisKey> found = repository.findByName("Searchable Hash");

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getRedisKey()).isNotNull();
    assertThat(found.get(0).getRedisKey()).isEqualTo("custom:hash:test-id-1");
  }

  @Test
  void testRedisKeyWithCustomPrefixOnPageQuery() {
    // Given
    for (int i = 0; i < 5; i++) {
      HashWithCustomPrefixAndRedisKey hash = new HashWithCustomPrefixAndRedisKey();
      hash.setId("test-id-" + i);
      hash.setName("Hash " + i);
      repository.save(hash);
    }

    // When
    Pageable pageable = PageRequest.of(0, 3);
    Page<HashWithCustomPrefixAndRedisKey> page = repository.findAll(pageable);

    // Then
    assertThat(page.getContent()).hasSize(3);
    assertThat(page.getContent()).allSatisfy(hash -> {
      assertThat(hash.getRedisKey()).isNotNull();
      assertThat(hash.getRedisKey()).startsWith("custom:hash:");
    });
  }
}