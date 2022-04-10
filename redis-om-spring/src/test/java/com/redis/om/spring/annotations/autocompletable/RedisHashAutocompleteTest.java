package com.redis.om.spring.annotations.autocompletable;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.bloom.fixtures.Person;
import com.redis.om.spring.annotations.bloom.fixtures.PersonRepository;

import io.redisearch.Suggestion;

public class RedisHashAutocompleteTest extends AbstractBaseEnhancedRedisTest {
  @Autowired
  PersonRepository repository;
  
  @BeforeEach
  public void loadPersons() {
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
  }
  
  @Test
  public void testBasicAutocomplete() {
    List<Suggestion> suggestions = repository.autoCompleteEmail("gu");
    List<String> suggestionsString = suggestions.stream().map(Suggestion::getString).collect(Collectors.toList());
    assertThat(suggestionsString).containsAll(List.of("guy.royse@redis.com", "guy.korland@redis.com"));
  }
}
