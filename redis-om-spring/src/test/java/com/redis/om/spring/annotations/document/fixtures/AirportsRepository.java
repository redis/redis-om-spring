package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.autocomplete.Suggestion;
import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteOptions;

import java.util.List;

@SuppressWarnings("unused") public interface AirportsRepository extends RedisDocumentRepository<Airport, String> {
  List<Suggestion> autoCompleteName(String query);

  List<Suggestion> autoCompleteName(String query, AutoCompleteOptions options);
}
