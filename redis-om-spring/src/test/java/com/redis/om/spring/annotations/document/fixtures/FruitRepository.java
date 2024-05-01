package com.redis.om.spring.annotations.document.fixtures;

import java.util.List;
import java.util.Optional;

import com.redis.om.spring.repository.RedisDocumentRepository;
//import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface FruitRepository extends RedisDocumentRepository<Fruit, Long> {
  Long deleteByName(String name);
  List<Fruit> removeByColor(String color);
  List<Fruit> deleteByColor(String color);
  Long removeByName(String name);
  long deleteByNameOrColor(String apple, String green);
  long deleteByColorIsNull();
}
