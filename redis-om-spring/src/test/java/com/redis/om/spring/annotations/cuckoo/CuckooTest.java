package com.redis.om.spring.annotations.cuckoo;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.hash.fixtures.Person2;
import com.redis.om.spring.annotations.hash.fixtures.Person2Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CuckooTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  Person2Repository repository;

  @BeforeEach
  void loadPersons() {
    Person2 guyr = Person2.of("Guy Royse", "guy.royse@redis.com", "guy");
    Person2 guyk = Person2.of("Guy Korland", "guy.korland@redis.com", "korland");
    Person2 simon = Person2.of("Simon Prickett", "simon@redis.com", "simon");
    Person2 justin = Person2.of("Justin Castilla", "justin@redis.com", "justin");
    Person2 steve = Person2.of("Steve Loretto", "steve.lorello@redis.com", "floridaman");
    Person2 kyleo = Person2.of("Kyle Owen", "kyle.owen@redis.com", "kyleo");
    Person2 kyleb = Person2.of("Kyle Banker", "kyle.banker@redis.com", "kyle");
    Person2 andrew = Person2.of("Andrew Brookins", "andrew.brookins@redis.com", "andrew");
    Person2 alex = Person2.of("Oleksandr Korolko", "aleksandr@redis.com", "alex");
    Person2 lance = Person2.of("Lance Leonard", "lancel@redis.com", "lance");
    Person2 rachel = Person2.of("Rachel Elledge", "rachel@redis.com", "rachel");
    Person2 kaitlyn = Person2.of("Kaitlyn Michael", "kaitlyn@redis.com", "kaitlyn");
    Person2 josefin = Person2.of("Josefin Sjoeberg", "josefin.sjoeberg@redis.com", "josefin");
    List<Person2> persons = List.of(guyr, guyk, simon, justin, steve, kyleo, kyleb, andrew, alex, lance, rachel,
      kaitlyn, josefin);

    repository.saveAll(persons);
  }

  @Test
  void testDynamicBloomRepositoryMethod() {
    assertTrue(repository.existsByEmail("kyle.owen@redis.com"));
    assertFalse(repository.existsByEmail("bsb@redis.com"));
  }

  @Test
  void testDynamicBloomRepositoryMethodForDefaultNamedFilter() {
    assertTrue(repository.existsByNickname("floridaman"));
    assertFalse(repository.existsByNickname("bsb"));
  }
}
