package com.redis.om.spring;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedisRepositoriesExcludeFilter implements AutoConfigurationImportFilter {

  private static final Set<String> SHOULD_SKIP = new HashSet<>(
    List.of("org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"));

  @Override
  public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
    boolean[] matches = new boolean[autoConfigurationClasses.length];

    for (int i = 0; i < autoConfigurationClasses.length; i++) {
      matches[i] = !SHOULD_SKIP.contains(autoConfigurationClasses[i]);
    }
    return matches;
  }

}
