package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.Movie;
import com.redis.om.spring.fixtures.document.repository.MovieRepository;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;

@SuppressWarnings(
  "SpellCheckingInspection"
)
class IdTests extends AbstractBaseDocumentTest {

  @Autowired
  MovieRepository movieRepository;

  @Autowired
  EntityStream entityStream;

  @BeforeEach
  void cleanUp() {
    movieRepository.deleteAll();

    Movie movie1 = new Movie();
    movie1.setId(805L);
    movie1.setNode("test");
    Movie movie2 = new Movie();
    movie2.setId(806L);
    movie2.setNode("test2");
    Movie movie3 = new Movie();
    movie3.setId(807L);
    movie3.setNode("test3");

    movieRepository.saveAll(Set.of(movie1, movie2, movie3));
  }

  @Test
  void testLongPrimivitiveIdIndexGeneration() {
    SearchStream<Movie> stream = entityStream.of(Movie.class);
    List<Movie> allMovies = stream.collect(Collectors.toList());
    assertThat(allMovies).hasSize(3);
  }
}
