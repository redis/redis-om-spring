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
import com.redis.om.spring.fixtures.hash.model.HashWithRedisKey;
import com.redis.om.spring.fixtures.hash.repository.HashWithRedisKeyRepository;

class RedisKeyHashTest extends AbstractBaseEnhancedRedisTest {
  
  @Autowired
  HashWithRedisKeyRepository repository;

  @BeforeEach
  void setup() {
    repository.deleteAll();
  }

  @Test
  void testRedisKeyPopulatedOnFindById() {
    // Given
    HashWithRedisKey hash = new HashWithRedisKey();
    hash.setId("test-id-1");
    hash.setName("Test Hash");
    hash.setDescription("Testing @RedisKey functionality");
    repository.save(hash);

    // When
    Optional<HashWithRedisKey> found = repository.findById("test-id-1");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getRedisKey()).isNotNull();
    assertThat(found.get().getRedisKey()).isEqualTo("com.redis.om.spring.fixtures.hash.model.HashWithRedisKey:test-id-1");
    assertThat(found.get().getName()).isEqualTo("Test Hash");
  }

  @Test
  void testRedisKeyPopulatedOnFindAll() {
    // Given
    HashWithRedisKey hash1 = new HashWithRedisKey();
    hash1.setId("test-id-1");
    hash1.setName("Hash 1");
    repository.save(hash1);

    HashWithRedisKey hash2 = new HashWithRedisKey();
    hash2.setId("test-id-2");
    hash2.setName("Hash 2");
    repository.save(hash2);

    // When
    List<HashWithRedisKey> all = repository.findAll();

    // Then
    assertThat(all).hasSize(2);
    assertThat(all).allSatisfy(hash -> {
      assertThat(hash.getRedisKey()).isNotNull();
      assertThat(hash.getRedisKey()).startsWith("com.redis.om.spring.fixtures.hash.model.HashWithRedisKey:");
    });
  }

  @Test
  void testRedisKeyPopulatedOnCustomQuery() {
    // Given
    HashWithRedisKey hash = new HashWithRedisKey();
    hash.setId("test-id-1");
    hash.setName("Searchable Hash");
    hash.setDescription("This should be searchable");
    repository.save(hash);

    // When
    List<HashWithRedisKey> found = repository.findByName("Searchable Hash");

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getRedisKey()).isNotNull();
    assertThat(found.get(0).getRedisKey()).isEqualTo("com.redis.om.spring.fixtures.hash.model.HashWithRedisKey:test-id-1");
  }

  @Test
  void testRedisKeyPopulatedOnPageQuery() {
    // Given
    for (int i = 0; i < 5; i++) {
      HashWithRedisKey hash = new HashWithRedisKey();
      hash.setId("test-id-" + i);
      hash.setName("Hash " + i);
      repository.save(hash);
    }

    // When
    Pageable pageable = PageRequest.of(0, 3);
    Page<HashWithRedisKey> page = repository.findAll(pageable);

    // Then
    assertThat(page.getContent()).hasSize(3);
    assertThat(page.getContent()).allSatisfy(hash -> {
      assertThat(hash.getRedisKey()).isNotNull();
      assertThat(hash.getRedisKey()).startsWith("com.redis.om.spring.fixtures.hash.model.HashWithRedisKey:");
    });
  }
}