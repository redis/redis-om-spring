package com.redis.om.spring.annotations;



import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface UseDialect {
  Dialect dialect() default Dialect.ONE;
}
