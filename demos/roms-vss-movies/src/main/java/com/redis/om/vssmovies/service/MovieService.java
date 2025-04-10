package com.redis.om.vssmovies.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.om.vssmovies.domain.Movie;
import com.redis.om.vssmovies.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class MovieService {

    private static final Logger log = LoggerFactory.getLogger(MovieService.class);
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final MovieRepository movieRepository;

    public MovieService(ObjectMapper objectMapper, ResourceLoader resourceLoader, MovieRepository movieRepository) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.movieRepository = movieRepository;
    }

    public void loadAndSaveMovies(String filePath) throws Exception {
        Resource resource = resourceLoader.getResource("classpath:" + filePath);

        log.info("Loading movies might take one or two minutes due to the creation of embeddings in the background.");
        log.info("Loading movies from: " + resource.getURI());
        try (InputStream is = resource.getInputStream()) {
            List<Movie> movies = objectMapper.readValue(is, new TypeReference<>() {});
            List<Movie> unprocessedMovies = movies.stream()
                    .filter(movie -> !movieRepository.existsById(movie.getTitle()) &&
                            movie.getYear() > 1980
                    ).toList();
            long systemMillis = System.currentTimeMillis();
            movieRepository.saveAll(unprocessedMovies);
            long elapsedMillis = System.currentTimeMillis() - systemMillis;
            log.info("Saved " + movies.size() + " movies in " + elapsedMillis + " ms");
        }
    }

    public boolean isDataLoaded() {
        return movieRepository.count() > 0;
    }
}