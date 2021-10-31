package com.redis.spring.annotations.bloom;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.spring.AbstractBaseEnhancedRedisTest;
import com.redis.spring.annotations.bloom.fixtures.Person;
import com.redis.spring.annotations.bloom.fixtures.PersonRepository;

public class BloomTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  PersonRepository repository;

  @Test
  public void testSaveOnePerson() {
    Person antirez = Person.of("Salvatore Sanfilippo", "antirez@redis.com");
    Person savedAntirez = repository.save(antirez);
    assertNotNull(savedAntirez.getId());
  }

  @Test
  public void testCustomBloomRepositoryMethod() {
    Person guyr = Person.of("Guy Royse", "guy.royse@redis.com");
    Person guyk = Person.of("Guy Korland", "guy.korland@redis.com");
    Person simon = Person.of("Simon Prickett", "simon@redis.com");
    Person justin = Person.of("Justin Castilla", "justin@redis.com");
    Person steve = Person.of("Steve Loretto", "steve.lorello@redis.com");
    Person kyleo = Person.of("Kyle Owen", "kyle.owen@redis.com");
    Person kyleb = Person.of("Kyle Banker", "kyle.banker@redis.com");
    Person andrew = Person.of("Andrew Brookins", "andrew.brookins@redis.com");
    Person alex = Person.of("Oleksandr Korolko", "aleksandr@redis.com");
    Person lance = Person.of("Lance Leonard", "lancel@redis.com");
    Person rachel = Person.of("Rachel Elledge", "rachel@redis.com");
    Person kaitlyn = Person.of("Kaitlyn Michael", "kaitlyn@redis.com");
    Person josefin = Person.of("Josefin Sjoeberg", "josefin.sjoeberg@redis.com");
    List<Person> persons = List.of(guyr, guyk, simon, justin, steve, kyleo, kyleb, andrew, alex, lance, rachel, kaitlyn,
        josefin);

    repository.saveAll(persons);

    assertTrue(repository.isEmailTaken("kyle.owen@redis.com"));
    assertFalse(repository.isEmailTaken("bsb@redis.com"));
  }

  @Test
  public void testDynamicBloomRepositoryMethod() {
    Person guyr = Person.of("Guy Royse", "guy.royse@redis.com");
    Person guyk = Person.of("Guy Korland", "guy.korland@redis.com");
    Person simon = Person.of("Simon Prickett", "simon@redis.com");
    Person justin = Person.of("Justin Castilla", "justin@redis.com");
    Person steve = Person.of("Steve Loretto", "steve.lorello@redis.com");
    Person kyleo = Person.of("Kyle Owen", "kyle.owen@redis.com");
    Person kyleb = Person.of("Kyle Banker", "kyle.banker@redis.com");
    Person andrew = Person.of("Andrew Brookins", "andrew.brookins@redis.com");
    Person alex = Person.of("Oleksandr Korolko", "aleksandr@redis.com");
    Person lance = Person.of("Lance Leonard", "lancel@redis.com");
    Person rachel = Person.of("Rachel Elledge", "rachel@redis.com");
    Person kaitlyn = Person.of("Kaitlyn Michael", "kaitlyn@redis.com");
    Person josefin = Person.of("Josefin Sjoeberg", "josefin.sjoeberg@redis.com");
    List<Person> persons = List.of(guyr, guyk, simon, justin, steve, kyleo, kyleb, andrew, alex, lance, rachel, kaitlyn,
        josefin);

    repository.saveAll(persons);

    assertTrue(repository.existsByEmail("kyle.owen@redis.com"));
    assertFalse(repository.existsByEmail("bsb@redis.com"));
  }
}
