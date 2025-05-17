package com.redis.om.spring.annotations.countmin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.SearchEvent;
import com.redis.om.spring.fixtures.hash.repository.SearchEventRepository;
import com.redis.om.spring.tuple.Tuples;

class CountMinTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  SearchEventRepository repository;

  @BeforeEach
  void loadSearchEvents() {
    SearchEvent event1 = SearchEvent.of("guy.royse@redis.com", "What's cache?", List.of("cache"), List.of(Tuples.of(
        "What's", 1L), Tuples.of("cache", 1L)));
    SearchEvent event2 = SearchEvent.of("guy.royse@redis.com", "What's new in Redis 8?", List.of("Redis"), List.of(
        Tuples.of("What's", 1L), Tuples.of("new", 1L), Tuples.of("in", 1L), Tuples.of("Redis", 1L), Tuples.of("8",
            1L)));
    SearchEvent event3 = SearchEvent.of("raphael.delio@redis.com", "Is Redis Search part of Redis 8?", List.of("Redis"),
        List.of(Tuples.of("Is", 1L), Tuples.of("Redis", 2L), Tuples.of("Search", 1L), Tuples.of("part", 1L), Tuples.of(
            "of", 1L), Tuples.of("8", 1L)));
    SearchEvent event4 = SearchEvent.of("kyle.banker@redis.com", "Is Redis the fastest vector database?", List.of(
        "database"), List.of(Tuples.of("Is", 1L), Tuples.of("Redis", 1L), Tuples.of("the", 1L), Tuples.of("fastest",
            1L), Tuples.of("vector", 1L), Tuples.of("database", 1L)));
    SearchEvent event5 = SearchEvent.of("brian.sambodden@redis.com", "What's cache?", List.of("cache"), List.of(Tuples
        .of("What's", 1L), Tuples.of("cache", 1L)));

    List<SearchEvent> persons = List.of(event1, event2, event3, event4, event5);

    repository.saveAll(persons);
  }

  @AfterEach
  void clear() {
    repository.deleteAll();
  }

  @Test
  void testCountSingleString() {
    long count = repository.getUserIdCountCustom("guy.royse@redis.com");
    assertEquals(2, count);

    long count2 = repository.getUserIdCountCustom("brian.sambodden@redis.com");
    assertEquals(1, count2);

    // Test non-existent value
    long count3 = repository.getUserIdCountCustom("salvatore.sanfilippo@redis.com");
    assertEquals(0, count3);
  }

  @Test
  void testCountSingleStringDynamic() {
    long count = repository.countBySearchSentence("What's cache?");
    assertEquals(2, count);

    long count2 = repository.countBySearchSentence("What's new in Redis 8?");
    assertEquals(1, count2);

    List<Long> count3 = repository.countBySearchSentence(List.of("What's cache?", "What's new in Redis 8?"));
    assertEquals(2, count3.get(0));
    assertEquals(1, count3.get(1));

    // Test non-existent value
    long count4 = repository.countBySearchSentence("Was Redis released in 2009?");
    assertEquals(0, count4);
  }

  @Test
  void testCountMultipleString() {
    long count = repository.countByHotTerms("Redis");
    assertEquals(2, count);

    long count2 = repository.countByHotTerms("cache");
    assertEquals(2, count2);

    List<Long> count3 = repository.countByHotTerms(List.of("Redis", "cache"));
    assertEquals(2, count3.get(0));
    assertEquals(2, count3.get(1));

    // Test non-existent value
    long count4 = repository.countByHotTerms("vector");
    assertEquals(0, count4);
  }

  @Test
  void testCountMultiplePair() {
    long count = repository.countBySearchWord("Redis");
    assertEquals(4, count);

    long count2 = repository.countBySearchWord("cache");
    assertEquals(2, count2);

    List<Long> count3 = repository.countBySearchWord(List.of("Redis", "cache"));
    assertEquals(4, count3.get(0));
    assertEquals(2, count3.get(1));

    // Test non-existent value
    long count4 = repository.countBySearchWord("Brazil");
    assertEquals(0, count4);
  }
}