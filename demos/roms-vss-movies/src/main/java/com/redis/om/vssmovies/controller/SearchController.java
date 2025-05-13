package com.redis.om.vssmovies.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.redis.om.spring.tuple.Pair;
import com.redis.om.vssmovies.domain.Movie;
import com.redis.om.vssmovies.service.SearchService;

@RestController
public class SearchController {

  private final SearchService searchService;

  public SearchController(SearchService searchService) {
    this.searchService = searchService;
  }

  @GetMapping(
    "/search"
  )
  public Map<String, Object> search(@RequestParam(
      required = false
  ) String text, @RequestParam(
      required = false
  ) Integer yearMin, @RequestParam(
      required = false
  ) Integer yearMax, @RequestParam(
      required = false
  ) List<String> cast, @RequestParam(
      required = false
  ) List<String> genres, @RequestParam(
      required = false
  ) Integer numberOfNearestNeighbors) {
    List<Pair<Movie, Double>> matchedMovies = searchService.search(text, yearMin, yearMax, cast, genres,
        numberOfNearestNeighbors);
    return Map.of("matchedMovies", matchedMovies, "count", matchedMovies.size());
  }
}
