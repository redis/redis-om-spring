package com.redis.om.spring.annotations.autocompletable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.util.DoubleComparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.google.common.io.Files;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.Airport;
import com.redis.om.spring.annotations.document.fixtures.AirportsRepository;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteOptions;

import io.redisearch.Suggestion;

class AutoCompleteTest extends AbstractBaseDocumentTest {

  @Autowired
  public AirportsRepository repository;

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @BeforeEach
  void loadAirports(@Value("classpath:/data/airport_codes.csv") File dataFile) throws IOException {
    List<Airport> data = Files //
        .readLines(dataFile, StandardCharsets.UTF_8) //
        .stream() //
        .map(l -> l.split(",")) //
        .map(ar -> Airport.of(ar[0], ar[1], ar[2])) //
        .collect(Collectors.toList());
    repository.saveAll(data);
  };

  @Test
  void deleteEntityShouldDeleteSuggestion() {
    String key = String.format("sugg:%s:%s", Airport.class.getSimpleName(), "name");
    SearchOperations<String> ops = modulesOperations.opsForSearch(key);
    long sugCountBefore = ops.getSuggestionLength();

    Pageable pageRequest = PageRequest.of(0, 1);
    Page<String> ids = repository.getIds(pageRequest);
    assertThat(ids).hasSize(1);
    String id = ids.getContent().get(0);
    Optional<Airport> maybeAirport = repository.findById(id);
    assertTrue(maybeAirport.isPresent());
    Airport airport = maybeAirport.get();
    repository.delete(airport);

    long sugCountAfter = ops.getSuggestionLength();
    assertTrue(sugCountAfter == sugCountBefore - 1);
  }

  @Test
  void deleteAllShouldDeleteAllSuggestion() {
    String key = String.format("sugg:%s:%s", Airport.class.getSimpleName(), "name");
    SearchOperations<String> ops = modulesOperations.opsForSearch(key);
    long sugCountBefore = ops.getSuggestionLength();
    assertTrue(sugCountBefore > 0);

    repository.deleteAll();

    long sugCountAfter = ops.getSuggestionLength();
    assertThat(sugCountAfter).isZero();
  }

  @Test
  void deleteEntityByIdShouldDeleteSuggestion() {
    String key = String.format("sugg:%s:%s", Airport.class.getSimpleName(), "name");
    SearchOperations<String> ops = modulesOperations.opsForSearch(key);
    long sugCountBefore = ops.getSuggestionLength();

    Pageable pageRequest = PageRequest.of(0, 1);
    Page<String> ids = repository.getIds(pageRequest);
    assertThat(ids).hasSize(1);
    String id = ids.getContent().get(0);
    repository.deleteById(id);

    long sugCountAfter = ops.getSuggestionLength();
    assertTrue(sugCountAfter == sugCountBefore - 1);
  }

  @Test
  void deleteAllEntitiesByIdShouldDeleteSuggestions() {
    String key = String.format("sugg:%s:%s", Airport.class.getSimpleName(), "name");
    SearchOperations<String> ops = modulesOperations.opsForSearch(key);
    long sugCountBefore = ops.getSuggestionLength();

    Pageable pageRequest = PageRequest.of(0, 3);
    List<String> ids = repository.getIds(pageRequest).getContent();
    assertThat(ids).hasSize(3);
    repository.deleteAllById(ids);

    long sugCountAfter = ops.getSuggestionLength();
    assertTrue(sugCountAfter == sugCountBefore - 3);
  }
  
  @Test
  void deleteAllEntitiesByCollectionShouldDeleteSuggestions() {
    String key = String.format("sugg:%s:%s", Airport.class.getSimpleName(), "name");
    SearchOperations<String> ops = modulesOperations.opsForSearch(key);
    long sugCountBefore = ops.getSuggestionLength();

    Pageable pageRequest = PageRequest.of(0, 3);
    List<String> ids = repository.getIds(pageRequest).getContent();
    assertThat(ids).hasSize(3);
    Iterable<Airport> airportsToDelete = repository.findAllById(ids);
    repository.deleteAll(airportsToDelete);

    long sugCountAfter = ops.getSuggestionLength();
    assertTrue(sugCountAfter == sugCountBefore - 3);
  }

  @Test
  void testGetAutocompleteSuggestions() {
    List<Suggestion> suggestions = repository.autoCompleteName("col");
    List<String> suggestionsString = suggestions.stream().map(Suggestion::getString).collect(Collectors.toList());
    assertThat(suggestionsString).containsAll(List.of("Columbia", "Columbus", "Colorado Springs"));
  }
  
  @Test
  void testGetAutocompleteSuggestionsFuzzy() {
    List<Suggestion> suggestions = repository.autoCompleteName("col", AutoCompleteOptions.get().fuzzy());
    List<String> suggestionsString = suggestions.stream().map(Suggestion::getString).collect(Collectors.toList());
    assertThat(suggestionsString).containsAll(List.of("Columbia", "Columbus", "Colorado Springs","Moline","Toledo"));
  }
  
  @Test
  void testGetAutocompleteSuggestionsWithLimit() {
    List<Suggestion> suggestions = repository.autoCompleteName("col", AutoCompleteOptions.get().limit(2));
    List<String> suggestionsString = suggestions.stream().map(Suggestion::getString).collect(Collectors.toList());
    assertAll(
      () -> assertThat(suggestionsString).size().isEqualTo(2),
      () ->assertThat(suggestionsString).containsAll(List.of("Columbia", "Columbus"))
    );
  }
  
  @Test
  void testGetAutocompleteSuggestionsWithPayload() {
    String columbusPayload = "{\"code\":\"CMH\",\"state\":\"OH\"}";
    String columbiaPayload = "{\"code\":\"CAE\",\"state\":\"SC\"}";
    String coloradoSpringsPayload = "{\"code\":\"COS\",\"state\":\"CO\"}";
    
    List<Suggestion> suggestions = repository.autoCompleteName("col", AutoCompleteOptions.get().withPayload());
    List<String> suggestionsString = suggestions.stream().map(Suggestion::getString).collect(Collectors.toList());
    List<Object> payloads = suggestions.stream().map(Suggestion::getPayload).collect(Collectors.toList());
    assertThat(suggestionsString).containsAll(List.of("Columbia", "Columbus", "Colorado Springs"));
    assertThat(payloads).containsAll(List.of(columbusPayload,columbiaPayload,coloradoSpringsPayload));
  }
  
  @Test
  void testGetAutocompleteSuggestionsWithScores() {    
    List<Suggestion> suggestions = repository.autoCompleteName("col", AutoCompleteOptions.get().withScore());
    List<String> suggestionsString = suggestions.stream().map(Suggestion::getString).collect(Collectors.toList());
    List<Double> scores = suggestions.stream().map(Suggestion::getScore).collect(Collectors.toList());
    assertThat(suggestionsString).containsAll(List.of("Columbia", "Columbus", "Colorado Springs"));
    
    assertThat(scores).usingComparatorForType(new DoubleComparator(0.1), Double.class).containsAll(List.of(0.41,0.41,0.27));
  }

}
