package com.redis.om.spring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

/**
 * Auto-configuration import filter that excludes default Redis repository auto-configuration.
 * <p>
 * This filter prevents Spring Boot's default Redis repositories auto-configuration from being
 * loaded, allowing Redis OM Spring to provide its own enhanced repository configuration instead.
 * <p>
 * The filter specifically excludes:
 * <ul>
 * <li>{@code RedisRepositoriesAutoConfiguration} - Standard Spring Data Redis repository configuration</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class RedisRepositoriesExcludeFilter implements AutoConfigurationImportFilter {

  /**
   * Default constructor for the repositories exclude filter.
   * <p>
   * This constructor is used by Spring Boot's auto-configuration mechanism
   * to create the filter instance.
   */
  public RedisRepositoriesExcludeFilter() {
    // Default constructor for Spring instantiation
  }

  private static final Set<String> SHOULD_SKIP = new HashSet<>(List.of(
      // Spring Boot 3.x class name
      "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
      // Spring Boot 4.0+ class name (modular autoconfigure)
      "org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration"));

  @Override
  public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
    boolean[] matches = new boolean[autoConfigurationClasses.length];

    for (int i = 0; i < autoConfigurationClasses.length; i++) {
      matches[i] = !SHOULD_SKIP.contains(autoConfigurationClasses[i]);
    }
    return matches;
  }

}
