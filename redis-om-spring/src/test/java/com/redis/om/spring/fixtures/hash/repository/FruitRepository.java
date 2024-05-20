package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.Fruit;
import com.redis.om.spring.repository.RedisEnhancedRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FruitRepository extends RedisEnhancedRepository<Fruit, Long> {
  Long deleteByName(String name);

  List<Fruit> removeByColor(String color);

  List<Fruit> deleteByColor(String color);

  Long removeByName(String name);

  long deleteByNameOrColor(String apple, String green);

  long deleteByColorIsNull();
}
