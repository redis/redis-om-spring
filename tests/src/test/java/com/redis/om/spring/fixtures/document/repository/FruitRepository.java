package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Fruit;
import com.redis.om.spring.repository.RedisDocumentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FruitRepository extends RedisDocumentRepository<Fruit, Long> {
  Long deleteByName(String name);

  List<Fruit> removeByColor(String color);

  List<Fruit> deleteByColor(String color);

  Long removeByName(String name);

  long deleteByNameOrColor(String apple, String green);

  long deleteByColorIsNull();
}
