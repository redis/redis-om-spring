package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation to configure indexing options for Redis OM entities.
 * This annotation allows customization of search index creation behavior,
 * including the index name, key prefix, and creation mode.
 *
 * <p>Supports Spring Expression Language (SpEL) for dynamic configuration:
 * <ul>
 * <li>Environment properties: #{&#64;environment.getProperty('app.tenant')}</li>
 * <li>Bean references: #{&#64;tenantResolver.currentTenant}</li>
 * <li>Method invocations: #{&#64;versionService.getVersion()}</li>
 * <li>Conditional logic: #{condition ? 'value1' : 'value2'}</li>
 * </ul>
 */
@Inherited
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.TYPE }
)
public @interface IndexingOptions {

  /**
   * Specifies the custom name for the search index. If not provided,
   * a default index name will be generated based on the entity class name.
   *
   * <p>Supports SpEL expressions for dynamic index naming:
   * <pre>
   * &#64;IndexingOptions(indexName = "#{&#64;environment.getProperty('app.tenant')}_idx")
   * &#64;IndexingOptions(indexName = "users_v#{&#64;versionService.getVersion()}")
   * </pre>
   *
   * @return the custom index name, or empty string to use default naming
   */
  String indexName() default "";

  /**
   * Specifies the custom key prefix for Redis keys. If not provided,
   * uses the default prefix from the entity annotation.
   *
   * <p>Supports SpEL expressions for dynamic key prefixes:
   * <pre>
   * &#64;IndexingOptions(keyPrefix = "#{&#64;tenantResolver.currentTenant}:")
   * &#64;IndexingOptions(keyPrefix = "#{&#64;environment.getProperty('app.prefix')}:")
   * </pre>
   *
   * @return the custom key prefix, or empty string to use default
   */
  String keyPrefix() default "";

  /**
   * Specifies the index creation mode that determines how the search index
   * should be created or updated.
   *
   * @return the index creation mode
   */
  IndexCreationMode creationMode() default IndexCreationMode.SKIP_IF_EXIST;
}
