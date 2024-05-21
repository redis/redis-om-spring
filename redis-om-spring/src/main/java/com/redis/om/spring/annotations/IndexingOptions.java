package com.redis.om.spring.annotations;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface IndexingOptions {
  String indexName() default "";
  IndexCreationMode creationMode() default IndexCreationMode.SKIP_IF_EXIST;
}
