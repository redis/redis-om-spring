package com.redis.om.spring.annotations.hash;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.Fruit;
import com.redis.om.spring.fixtures.hash.repository.FruitRepository;

class RepositoryDeleteTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  private FruitRepository fruitRepository;

  @BeforeEach
  void beforeEach() {
    Fruit apple = Fruit.of(1, "apple", "red");
    Fruit custardApple = Fruit.of(2, "custard apple", "green");
    Fruit mango = Fruit.of(3, "mango", "yellow");
    Fruit guava = Fruit.of(4, "guava", "green");
    Fruit unknown = Fruit.of(5, "unknown", null);
    fruitRepository.saveAll(List.of(apple, custardApple, mango, guava, unknown));
  }

  @Test
  public void givenFruits_WhenDeletedByName_ThenDeletedFruitCountShouldReturn() {
    long deletedFruitCount = fruitRepository.deleteByName("apple");

    assertThat(deletedFruitCount).isEqualTo(1);
    assertTrue(true);
  }

  @Test
  public void givenFruits_WhenRemovedByColor_ThenDeletedFruitsShouldReturn() {
    List<Fruit> fruits = fruitRepository.removeByColor("green");

    assertThat(fruits).hasSize(2);
    assertThat(fruits).extracting(Fruit::getColor).containsOnly("green");
  }

  @Test
  public void givenFruits_WhenRemovedByName_ThenDeletedFruitCountShouldReturn() {
    long deletedFruitCount = fruitRepository.removeByName("apple");

    assertThat(deletedFruitCount).isEqualTo(1);
  }

  @Test
  public void givenFruits_WhenDeletedByColorOrName_ThenDeletedFruitsShouldReturn() {
    long deletedCount = fruitRepository.deleteByNameOrColor("apple", "green");

    assertThat(deletedCount).isEqualTo(3);
  }

  @Test
  public void givenFruits_WhenDeletedByColorIsNull_ThenDeletedFruitsShouldReturn() {
    long deletedCount = fruitRepository.deleteByColorIsNull();

    assertThat(deletedCount).isEqualTo(1);
  }
}