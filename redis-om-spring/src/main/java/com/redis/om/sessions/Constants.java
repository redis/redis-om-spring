/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Constants {
  public static final String CREATED_AT_KEY = "createdAt";
  public static final String MAX_INACTIVE_INTERVAL_KEY = "maxInactiveInterval";
  public static final String LAST_ACCESSED_TIME_KEY = "lastAccessedTime";
  public static final String LAST_MODIFIED_TIME_KEY = "lastModifiedTime";
  public static final String SESSION_METRICS_KEY = "sessionMetrics";
  public static final String SIZE_FIELD_NAME = "sessionSize";
  public static final String INVALIDATION_CHANNEL_FORMAT = "redis-session-invalidate:%s";
  public static final Set<String> reservedFields = new HashSet<>(Arrays.asList(Constants.CREATED_AT_KEY,
      Constants.SIZE_FIELD_NAME, Constants.MAX_INACTIVE_INTERVAL_KEY, Constants.LAST_ACCESSED_TIME_KEY,
      Constants.LAST_MODIFIED_TIME_KEY));
}
