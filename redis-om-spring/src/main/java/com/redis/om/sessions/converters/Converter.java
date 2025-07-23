/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.converters;

public interface Converter<T> {

  T parse(String s);

  String toRedisString(T o);

}
