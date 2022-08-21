package com.redis.om.spring.annotations.hash.fixtures;

import java.time.LocalDate;
import java.util.List;

import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface KitchenSinkRepository extends RedisEnhancedRepository<KitchenSink, String> {
  List<KitchenSink> findByLocalDateGreaterThan(LocalDate localDate);
}
