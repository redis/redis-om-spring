package com.redis.om.vssmovies.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.tuple.Fields;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.vectorize.Embedder;
import com.redis.om.vssmovies.domain.Movie;
import com.redis.om.vssmovies.domain.Movie$;

@Service
public class SearchService {

  private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
  private final EntityStream entityStream;
  private final Embedder embedder;

  public SearchService(EntityStream entityStream, Embedder embedder) {
    this.entityStream = entityStream;
    this.embedder = embedder;
  }

  public List<Pair<Movie, Double>> search(String query, Integer yearMin, Integer yearMax, List<String> cast,
      List<String> genres, Integer numberOfNearestNeighbors) {
    logger.info("Received text: {}", query);
    logger.info("Received yearMin: {} yearMax: {}", yearMin, yearMax);
    logger.info("Received cast: {}", cast);
    logger.info("Received genres: {}", genres);

    if (numberOfNearestNeighbors == null)
      numberOfNearestNeighbors = 3;
    if (yearMin == null)
      yearMin = 1900;
    if (yearMax == null)
      yearMax = 2100;

    byte[] embeddedQuery = embedder.getTextEmbeddingsAsBytes(List.of(query), Movie$.EXTRACT).getFirst();

    SearchStream<Movie> stream = entityStream.of(Movie.class);
    return stream.filter(Movie$.EMBEDDED_EXTRACT.knn(numberOfNearestNeighbors, embeddedQuery)).filter(Movie$.YEAR
        .between(yearMin, yearMax)).filter(Movie$.CAST.eq(cast)).filter(Movie$.GENRES.eq(genres)).sorted(
            Movie$._EMBEDDED_EXTRACT_SCORE).map(Fields.of(Movie$._THIS, Movie$._EMBEDDED_EXTRACT_SCORE)).collect(
                Collectors.toList());
  }
}
