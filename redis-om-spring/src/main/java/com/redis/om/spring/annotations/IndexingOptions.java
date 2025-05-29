package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation to configure indexing options for Redis OM entities.
 * This annotation allows customization of search index creation behavior,
 * including the index name and creation mode.
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
   * @return the custom index name, or empty string to use default naming
   */
  String indexName() default "";

  /**
   * Specifies the index creation mode that determines how the search index
   * should be created or updated.
   *
   * @return the index creation mode
   */
  IndexCreationMode creationMode() default IndexCreationMode.SKIP_IF_EXIST;
}
