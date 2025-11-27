package com.redis.om.multitenant.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redis.om.spring.indexing.EphemeralIndexService;
import com.redis.om.spring.indexing.IndexMigrationService;
import com.redis.om.spring.indexing.RediSearchIndexer;

/**
 * Configuration for dynamic indexing features.
 *
 * <p>Registers beans required for advanced indexing capabilities:
 * <ul>
 * <li>EphemeralIndexService - for temporary indexes with TTL
 * <li>IndexMigrationService - for blue-green index migrations
 * </ul>
 */
@Configuration
public class DynamicIndexingConfig {

  @Bean
  public EphemeralIndexService ephemeralIndexService(RediSearchIndexer indexer) {
    return new EphemeralIndexService(indexer);
  }

  @Bean
  public IndexMigrationService indexMigrationService(RediSearchIndexer indexer, ApplicationContext applicationContext) {
    return new IndexMigrationService(indexer, applicationContext);
  }
}
