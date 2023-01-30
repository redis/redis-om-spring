package com.redis.om.spring.annotations.autocompletable;

import com.google.common.io.Files;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.Airport;
import com.redis.om.spring.annotations.document.fixtures.AirportsRepository;
import com.redis.om.spring.autocomplete.Suggestion;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteOptions;
import org.assertj.core.util.DoubleComparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("SpellCheckingInspection")
class AutoCompleteTest extends AbstractBaseDocumentTest {

  @Autowired
  AirportsRepository repository;

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @BeforeEach
  void loadAirports(@Value("classpath:/data/airport_codes.csv") File dataFile) throws IOException {
    if (repository.count() != 190) {
      List<Airport> data = Files //
          .readLines(dataFile, StandardCharsets.UTF_8) //
          .stream() //
          .map(l -> l.split(",")) //
          .map(ar -> Airport.of(ar[0], ar[1], ar[2])) //
          .collect(Collectors.toList());
      repository.saveAll(data);
    }
  }

  @Test
  void testGetAutocompleteSuggestions() {
    List<Suggestion> suggestions = repository.autoCompleteName("col");
    List<String> suggestionsString = suggestions.stream().map(Suggestion::getValue).collect(Collectors.toList());
    assertThat(suggestionsString).containsAll(List.of("Columbia", "Columbus", "Colorado Springs"));
  }

  @Test
  void testGetAutocompleteSuggestionsFuzzy() {
    List<Suggestion> suggestions = repository.autoCompleteName("col", AutoCompleteOptions.get().fuzzy());
    List<String> suggestionsString = suggestions.stream().map(Suggestion::getValue).collect(Collectors.toList());
    assertThat(suggestionsString).containsAll(List.of("Columbia", "Columbus", "Colorado Springs", "Moline", "Toledo"));
  }

  @Test
  void testGetAutocompleteSuggestionsWithLimit() {
    List<Suggestion> suggestions = repository.autoCompleteName("col", AutoCompleteOptions.get().limit(2));
    List<String> suggestionsString = suggestions.stream().map(Suggestion::getValue).collect(Collectors.toList());
    assertAll(() -> assertThat(suggestionsString).size().isEqualTo(2),
        () -> assertThat(suggestionsString).containsAll(List.of("Columbia", "Columbus")));
  }

  @Test
  void testGetAutocompleteSuggestionsWithPayload() {
    Map<String, Object> columbusPayload = Map.of("code", "CMH", "state", "OH");
    Map<String, Object> columbiaPayload = Map.of("code", "CAE", "state", "SC");
    Map<String, Object> coloradoSpringsPayload = Map.of("code", "COS", "state", "CO");

    List<Suggestion> suggestions = repository.autoCompleteName("col", AutoCompleteOptions.get().withPayload());
    List<String> suggestionsString = suggestions.stream().map(Suggestion::getValue).collect(Collectors.toList());
    List<Map<String, Object>> payloads = suggestions.stream().map(Suggestion::getPayload).collect(Collectors.toList());
    assertThat(suggestionsString).containsAll(List.of("Columbia", "Columbus", "Colorado Springs"));
    assertThat(payloads).hasSize(3).contains(columbusPayload, columbiaPayload, coloradoSpringsPayload);
  }

  @Test
  void testGetAutocompleteSuggestionsWithScores() {
    List<Suggestion> suggestions = repository.autoCompleteName("col", AutoCompleteOptions.get().withScore());
    List<String> suggestionsString = suggestions.stream().map(Suggestion::getValue).collect(Collectors.toList());
    List<Double> scores = suggestions.stream().map(Suggestion::getScore).collect(Collectors.toList());
    assertThat(suggestionsString).containsAll(List.of("Columbia", "Columbus", "Colorado Springs"));

    assertThat(scores).usingComparatorForType(new DoubleComparator(0.1), Double.class)
        .containsAll(List.of(0.41, 0.41, 0.27));
  }

}
