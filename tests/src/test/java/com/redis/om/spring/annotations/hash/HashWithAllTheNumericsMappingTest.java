package com.redis.om.spring.annotations.hash;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.HashWithAllTheNumerics;
import com.redis.om.spring.fixtures.hash.repository.HashWithAllTheNumericsRepository;
import com.redis.om.spring.search.stream.EntityStream;

@SuppressWarnings(
  "SpellCheckingInspection"
)
class HashWithAllTheNumericsMappingTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  HashWithAllTheNumericsRepository repo;

  @Autowired
  EntityStream es;

  @BeforeEach
  void createTestDataIfNeeded() {
    HashWithAllTheNumerics hwatn1 = HashWithAllTheNumerics.of("hash1", 1.0f, 1.0, BigInteger.valueOf(1), BigDecimal
        .valueOf(1.0));
    HashWithAllTheNumerics hwatn2 = HashWithAllTheNumerics.of("hash2", 2.0f, 2.0, BigInteger.valueOf(2), BigDecimal
        .valueOf(2.0));
    HashWithAllTheNumerics hwatn3 = HashWithAllTheNumerics.of("hash3", 3.0f, 3.0, BigInteger.valueOf(3), BigDecimal
        .valueOf(3.0));
    HashWithAllTheNumerics hwatn4 = HashWithAllTheNumerics.of("hash4", 4.0f, 4.0, BigInteger.valueOf(4), BigDecimal
        .valueOf(4.0));
    HashWithAllTheNumerics hwatn5 = HashWithAllTheNumerics.of("hash5", 5.0f, 5.0, BigInteger.valueOf(5), BigDecimal
        .valueOf(5.0));
    HashWithAllTheNumerics hwatn6 = HashWithAllTheNumerics.of("hash6", 6.0f, 6.0, BigInteger.valueOf(6), BigDecimal
        .valueOf(6.0));
    repo.saveAll(Set.of(hwatn1, hwatn2, hwatn3, hwatn4, hwatn5, hwatn6));
  }

  @Test
  void testValuesAreStoredCorrectly() {
    for (int i = 1; i <= 6; i++) {
      String id = "hash" + i;
      Optional<HashWithAllTheNumerics> hwatn = repo.findById(id);

      assertThat(hwatn).isPresent();

      HashWithAllTheNumerics entity = hwatn.get();
      assertThat(entity.getId()).isEqualTo(id);
      assertThat(entity.getAfloat()).isEqualTo((float) i);
      assertThat(entity.getAdouble()).isEqualTo((double) i);
      assertThat(entity.getAbigInteger()).isEqualTo(BigInteger.valueOf(i));
      assertThat(entity.getAbigDecimal()).isEqualTo(new BigDecimal(i + ".0"));
    }
  }

  @Test
  void testFindByAFloatBetween() {
    Iterable<HashWithAllTheNumerics> result = repo.findByAfloatBetween(2.5f, 4.5f);
    assertThat(result).hasSize(2);
    assertThat(result).extracting(HashWithAllTheNumerics::getAfloat).containsExactlyInAnyOrder(3.0f, 4.0f);
  }

  @Test
  void testFindByADoubleBetween() {
    Iterable<HashWithAllTheNumerics> result = repo.findByAdoubleBetween(3.5, 5.5);

    assertThat(result).hasSize(2);
    assertThat(result).extracting(HashWithAllTheNumerics::getAdouble).containsExactlyInAnyOrder(4.0, 5.0);
  }

  @Test
  void testFindByABigDecimalBetween() {
    Iterable<HashWithAllTheNumerics> result = repo.findByAbigDecimalBetween(new BigDecimal("1.5"), new BigDecimal(
        "3.5"));

    assertThat(result).hasSize(2);
    assertThat(result).extracting(HashWithAllTheNumerics::getAbigDecimal).containsExactlyInAnyOrder(new BigDecimal(
        "2.0"), new BigDecimal("3.0"));
  }

  @Test
  void testFindByABigIntegerBetween() {
    Iterable<HashWithAllTheNumerics> result = repo.findByAbigIntegerBetween(BigInteger.valueOf(4), BigInteger.valueOf(
        6));

    assertThat(result).hasSize(3);
    assertThat(result).extracting(HashWithAllTheNumerics::getAbigInteger).containsExactlyInAnyOrder(BigInteger.valueOf(
        4), BigInteger.valueOf(5), BigInteger.valueOf(6));
  }
}
