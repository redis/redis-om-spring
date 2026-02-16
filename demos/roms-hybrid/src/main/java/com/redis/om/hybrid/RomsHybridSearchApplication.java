package com.redis.om.hybrid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;

/**
 * Redis OM Spring Hybrid Search Demo Application.
 *
 * Demonstrates hybrid search combining full-text search (BM25) with
 * vector similarity search for enhanced product discovery.
 */
@SpringBootApplication
@EnableRedisEnhancedRepositories(
    basePackages = "com.redis.om.hybrid.repositories"
)
public class RomsHybridSearchApplication {

  public static void main(String[] args) {
    SpringApplication.run(RomsHybridSearchApplication.class, args);
  }
}
