package com.redis.om.streams.annotation;

import com.redis.om.streams.config.RedisStreamConsumerRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RedisStreamConsumerRegistrar.class)
public @interface EnableRedisStreams {

    /**
     * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations e.g.:
     * {@code @EnableRedisRepositories("org.my.pkg")} instead of
     * {@code @EnableRedisRepositories(basePackages="org.my.pkg")}.
     *
     * @return basePackages
     */
    @AliasFor(
            "basePackages"
    )
    String[] value() default {};

    /**
     * Base packages to scan for annotated components. {@link #value()} is an alias for (and mutually exclusive with) this
     * attribute.
     *
     * @return basePackages as a String
     */
    @AliasFor(
            "value"
    )
    String[] basePackages() default {};

}
