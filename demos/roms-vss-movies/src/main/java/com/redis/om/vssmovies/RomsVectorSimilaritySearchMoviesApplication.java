package com.redis.om.vssmovies;

import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;
import com.redis.om.vssmovies.service.MovieService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@EnableRedisEnhancedRepositories(basePackages = {"com.redis.om.vssmovies*"})
public class RomsVectorSimilaritySearchMoviesApplication {

    public static void main(String[] args) {
        SpringApplication.run(RomsVectorSimilaritySearchMoviesApplication.class, args);
    }

    @Bean
    CommandLineRunner loadData(
            MovieService movieService) {
        return args -> {
            if (movieService.isDataLoaded()) {
                System.out.println("Data already loaded. Skipping data load.");
                return;
            }
            movieService.loadAndSaveMovies("movies.json");
        };
    }

}
