package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Movie;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface MovieRepository extends RedisDocumentRepository<Movie, Long> {
}
