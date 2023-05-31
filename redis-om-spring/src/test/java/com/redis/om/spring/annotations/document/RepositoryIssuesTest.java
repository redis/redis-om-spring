package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.SKU;
import com.redis.om.spring.annotations.document.fixtures.SKUCacheRepository;
import com.redis.om.spring.annotations.document.fixtures.User2;
import com.redis.om.spring.annotations.document.fixtures.User2Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("SpellCheckingInspection")
class RepositoryIssuesTest extends AbstractBaseDocumentTest {
  @Autowired
  User2Repository repository;

  @Autowired
  SKUCacheRepository skuCacheRepository;

  @BeforeEach
  void cleanUp() {
    repository.deleteAll();
    repository.save(User2.of("Doe", "Paris", "12 rue Rivoli"));

    skuCacheRepository.deleteAll();
    List<SKU> skuCaches = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      skuCaches.add(
          new SKU((long) i, "A" + i + i + i + i + i, "SKU " + i));
    }
    skuCacheRepository.saveAll(skuCaches);

  }

  // RediSearchQuery wrong preparedQuery #187
  @Test
  void testIncorrectParameterInjection() {
    Iterable<User2> results = repository.findUser("Doe", "Paris", "12 rue Rivoli");

    assertThat(results).extracting("name").containsExactly("Doe");
  }

  @Test
  void testFindAllByText() {
    List<SKU> result = skuCacheRepository.findAllBySkuNameIn(Set.of("SKU 1", "SKU 2"));

    assertAll( //
        () -> assertThat(result).hasSize(2),
        () -> assertThat(result).extracting("skuName").containsExactlyInAnyOrder("SKU 1", "SKU 2") //
    );
  }

  @Test
  void testFindAllByTag() {
    List<SKU> result = skuCacheRepository.findAllBySkuNumberIn(Set.of("A11111","A00000"));

    assertAll( //
        () -> assertThat(result).hasSize(2),
        () -> assertThat(result).extracting("skuNumber").containsExactlyInAnyOrder("A11111","A00000") //
    );
  }

  @Test
  void testFindOneBy() {
    SKU result = skuCacheRepository.findOneBySkuNumber("A11111").orElseThrow();

    assertAll( //
        () -> assertThat(result).isNotNull(),
        () -> assertThat(result.getSkuNumber()).isEqualTo("A11111") //
    );
  }
}
