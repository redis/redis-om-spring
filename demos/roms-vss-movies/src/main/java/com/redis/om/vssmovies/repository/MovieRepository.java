package com.redis.om.vssmovies.repository;

import com.redis.om.spring.repository.RedisEnhancedRepository;
import com.redis.om.vssmovies.domain.Movie;

public interface MovieRepository extends RedisEnhancedRepository<Movie, String> {
}