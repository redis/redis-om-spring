package com.redis.om.spring.annotations.bloom;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.hash.fixtures.Person;
import com.redis.om.spring.annotations.hash.fixtures.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpellCheckingInspection") class BloomTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  PersonRepository repository;
  
  @BeforeEach
  void loadPersons() {
    Person guyr = Person.of("Guy Royse", "guy.royse@redis.com", "guy");
    Person guyk = Person.of("Guy Korland", "guy.korland@redis.com", "korland");
    Person simon = Person.of("Simon Prickett", "simon@redis.com", "simon");
    Person justin = Person.of("Justin Castilla", "justin@redis.com", "justin");
    Person steve = Person.of("Steve Loretto", "steve.lorello@redis.com", "floridaman");
    Person kyleo = Person.of("Kyle Owen", "kyle.owen@redis.com", "kyleo");
    Person kyleb = Person.of("Kyle Banker", "kyle.banker@redis.com", "kyle");
    Person andrew = Person.of("Andrew Brookins", "andrew.brookins@redis.com", "andrew");
    Person alex = Person.of("Oleksandr Korolko", "aleksandr@redis.com", "alex");
    Person lance = Person.of("Lance Leonard", "lancel@redis.com", "lance");
    Person rachel = Person.of("Rachel Elledge", "rachel@redis.com", "rachel");
    Person kaitlyn = Person.of("Kaitlyn Michael", "kaitlyn@redis.com", "kaitlyn");
    Person josefin = Person.of("Josefin Sjoeberg", "josefin.sjoeberg@redis.com", "josefin");
    List<Person> persons = List.of(guyr, guyk, simon, justin, steve, kyleo, kyleb, andrew, alex, lance, rachel, kaitlyn,
        josefin);

    repository.saveAll(persons);
  }

  @Test
  void testCustomBloomRepositoryMethod() {
    assertTrue(repository.isEmailTaken("kyle.owen@redis.com"));
    assertFalse(repository.isEmailTaken("bsb@redis.com"));
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
