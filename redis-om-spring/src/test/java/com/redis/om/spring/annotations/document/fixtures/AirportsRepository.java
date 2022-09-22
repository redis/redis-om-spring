package com.redis.om.spring.annotations.document.fixtures;

import java.util.List;

import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteOptions;

public interface AirportsRepository extends RedisDocumentRepository<Airport, String> {
  List<String> autoCompleteName(String query);
  List<String> autoCompleteName(String query, AutoCompleteOptions options);
}
