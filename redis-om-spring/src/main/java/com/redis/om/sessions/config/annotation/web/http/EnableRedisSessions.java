/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.config.annotation.web.http;

import java.lang.annotation.*;

import org.springframework.context.annotation.Import;

@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  ElementType.TYPE
)
@Documented
@Import(
  RedisSessionsConfiguration.class
)
public @interface EnableRedisSessions {
}
