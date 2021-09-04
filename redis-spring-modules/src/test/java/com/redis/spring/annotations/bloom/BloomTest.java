package com.redis.spring.annotations.bloom;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.spring.AbstractBaseTest;
import com.redis.spring.annotations.bloom.fixtures.Person;
import com.redis.spring.annotations.bloom.fixtures.PersonRepository;

public class BloomTest extends AbstractBaseTest {

  @Autowired
  PersonRepository repository;

  @Test
  public void testSaveOnePerson() {
    Person antirez = Person.of("Salvatore Sanfilippo", "antirez@redislabs.com");
    repository.save(antirez);
  }

  @Test
  public void testBasicCrudOperations() {
    Person guyr = Person.of("Guy Royse", "guy.royse@redislabs.com");
    Person guyk = Person.of("Guy Korland", "guy.korland@redislabs.com");
    Person simon = Person.of("Simon Prickett", "simon@redislabs.com");
    Person justin = Person.of("Justin Castilla", "justin@redislabs.com");
    Person steve = Person.of("Steve Loretto", "steve.lorello@redislabs.com");
    Person kyleo = Person.of("Kyle Owen", "kyle.owen@redislabs.com");
    Person kyleb = Person.of("Kyle Banker", "kyle.banker@redislabs.com");
    Person andrew = Person.of("Andrew Brookins", "andrew.brookins@redislabs.com");
    Person alex = Person.of("Oleksandr Korolko", "aleksandr@redislabs.com");
    Person lance = Person.of("Lance Leonard", "lancel@redislabs.com");
    Person rachel = Person.of("Rachel Elledge", "rachel@redislabs.com");
    Person kaitlyn = Person.of("Kaitlyn Michael", "kaitlyn@redislabs.com");
    Person josefin = Person.of("Josefin Sjoeberg", "josefin.sjoeberg@redislabs.com");
    List<Person> persons = List.of(guyr, guyk, simon, justin, steve, kyleo, kyleb, andrew, alex, lance, rachel, kaitlyn,
        josefin);

    repository.saveAll(persons);

    assertTrue(repository.isEmailTaken("kyle.owen@redislabs.com"));
    assertFalse(repository.isEmailTaken("bsb@redislabs.com"));
  }
}
