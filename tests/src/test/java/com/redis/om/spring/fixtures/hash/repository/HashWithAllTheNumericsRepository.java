package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.HashWithAllTheNumerics;
import com.redis.om.spring.repository.RedisEnhancedRepository;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface HashWithAllTheNumericsRepository extends RedisEnhancedRepository<HashWithAllTheNumerics, String> {
  Iterable<HashWithAllTheNumerics> findByAfloatBetween(Float low, Float high);
  Iterable<HashWithAllTheNumerics> findByAdoubleBetween(Double low, Double high);
  Iterable<HashWithAllTheNumerics> findByAbigDecimalBetween(BigDecimal low, BigDecimal high);
  Iterable<HashWithAllTheNumerics> findByAbigIntegerBetween(BigInteger low, BigInteger high);
}



