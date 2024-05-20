package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.KitchenSink;
import com.redis.om.spring.repository.RedisEnhancedRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public interface KitchenSinkRepository extends RedisEnhancedRepository<KitchenSink, String> {
  List<KitchenSink> findByLocalDateGreaterThan(LocalDate localDate);

  Optional<KitchenSink> findFirstByName(String name);
}
