package com.redis.om.streams.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisStreamConsumer {
    String topicName();
    String groupName();
    String consumerName() default "";
    boolean autoAck() default false;
    boolean cluster() default false;
}