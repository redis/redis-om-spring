package com.redis.om.spring.serialization.gson;

import com.google.gson.GsonBuilder;

/**
 * Callback interface that can be used to customize a {@link GsonBuilder}.
 * <p>
 * This interface replaces the Spring Boot GsonBuilderCustomizer which was moved
 * to a separate module in Spring Boot 4.0. It provides the same functionality
 * for customizing GsonBuilder instances in Redis OM Spring.
 *
 * @since 2.0.0
 */
@FunctionalInterface
public interface GsonBuilderCustomizer {

  /**
   * Customize the given {@link GsonBuilder}.
   *
   * @param gsonBuilder the builder to customize
   */
  void customize(GsonBuilder gsonBuilder);
}
