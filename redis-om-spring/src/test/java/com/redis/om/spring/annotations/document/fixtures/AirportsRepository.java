package com.redis.om.spring.annotations.document.fixtures;

import java.util.List;

import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteOptions;

import io.redisearch.Suggestion;

public interface AirportsRepository extends RedisDocumentRepository<Airport, String> {
  List<Suggestion> autoCompleteName(String query);
  List<Suggestion> autoCompleteName(String query, AutoCompleteOptions options);
}
