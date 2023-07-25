package com.redis.om.spring.annotations.document;

import com.google.common.collect.Sets;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.*;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import redis.clients.jedis.json.Path;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
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
