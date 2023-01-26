package com.redis.om.spring.annotations.autocompletable;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.Airport;
import com.redis.om.spring.annotations.document.fixtures.AirportsRepository;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpellCheckingInspection")
class AutoCompleteDeletionTest extends AbstractBaseDocumentTest {

  @Autowired
  AirportsRepository repository;

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @BeforeEach
  void loadAirports() {
    repository.deleteAll();
    repository.saveAll(List.of( //
        Airport.of("Huntsville International Airport", "HSV", "AL"), //
        Airport.of("Mobile", "MOB", "AL"), //
        Airport.of("Montgomery", "MGM", "AL"), //
        Airport.of("Anchorage International Airport", "ANC", "AK"), //
        Airport.of("Fairbanks International Airport", "FAI", "AK"), //
        Airport.of("Juneau International Airport", "JNU", "AK"), //
        Airport.of("Flagstaff", "FLG", "AZ"), //
        Airport.of("Phoenix, Phoenix Sky Harbor International Airport", "PHX", "AZ") //
    ));
  }

  @Test
  void deleteEntityShouldDeleteSuggestion() {
    String key = String.format("sugg:%s:%s", Airport.class.getSimpleName(), "name");
    SearchOperations<String> ops = modulesOperations.opsForSearch(key);
    long sugCountBefore = ops.getSuggestionLength(key);

    Pageable pageRequest = PageRequest.of(0, 1);
    Page<String> ids = repository.getIds(pageRequest);
    assertThat(ids).hasSize(1);
    String id = ids.getContent().get(0);
    Optional<Airport> maybeAirport = repository.findById(id);
    assertTrue(maybeAirport.isPresent());
    Airport airport = maybeAirport.get();
    repository.delete(airport);

    long sugCountAfter = ops.getSuggestionLength(key);
    assertEquals(sugCountAfter, sugCountBefore - 1);
  }

  @Test
  void deleteAllShouldDeleteAllSuggestion() {
    String key = String.format("sugg:%s:%s", Airport.class.getSimpleName(), "name");
    SearchOperations<String> ops = modulesOperations.opsForSearch(key);
    long sugCountBefore = ops.getSuggestionLength(key);
    assertTrue(sugCountBefore > 0);

    repository.deleteAll();

    long sugCountAfter = ops.getSuggestionLength(key);
    assertThat(sugCountAfter).isZero();
  }

  @Test
  void deleteEntityByIdShouldDeleteSuggestion() {
    String key = String.format("sugg:%s:%s", Airport.class.getSimpleName(), "name");
    SearchOperations<String> ops = modulesOperations.opsForSearch(key);
    long sugCountBefore = ops.getSuggestionLength(key);

    Pageable pageRequest = PageRequest.of(0, 1);
    Page<String> ids = repository.getIds(pageRequest);
    assertThat(ids).hasSize(1);
    String id = ids.getContent().get(0);
    repository.deleteById(id);

    long sugCountAfter = ops.getSuggestionLength(key);
    assertEquals(sugCountAfter, sugCountBefore - 1);
  }

  @Test
  void deleteAllEntitiesByIdShouldDeleteSuggestions() {
    String key = String.format("sugg:%s:%s", Airport.class.getSimpleName(), "name");
    SearchOperations<String> ops = modulesOperations.opsForSearch(key);
    long sugCountBefore = ops.getSuggestionLength(key);

    Pageable pageRequest = PageRequest.of(0, 3);
    List<String> ids = repository.getIds(pageRequest).getContent();
    assertThat(ids).hasSize(3);
    repository.deleteAllById(ids);

    long sugCountAfter = ops.getSuggestionLength(key);
    assertEquals(sugCountAfter, sugCountBefore - 3);
  }

  @Test
  void deleteAllEntitiesByCollectionShouldDeleteSuggestions() {
    String key = String.format("sugg:%s:%s", Airport.class.getSimpleName(), "name");
    SearchOperations<String> ops = modulesOperations.opsForSearch(key);
    long sugCountBefore = ops.getSuggestionLength(key);

    Pageable pageRequest = PageRequest.of(0, 3);
    List<String> ids = repository.getIds(pageRequest).getContent();
    assertThat(ids).hasSize(3);
    Iterable<Airport> airportsToDelete = repository.findAllById(ids);
    repository.deleteAll(airportsToDelete);

    long sugCountAfter = ops.getSuggestionLength(key);
    assertEquals(sugCountAfter, sugCountBefore - 3);
  }
}
