/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.config.annotation.web.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.redis.om.sessions.RedisSessionProvider;

@Component
public class StartupBean {
  @Autowired
  private RedisSessionProvider provider;

  public void createIndex() {
    try {
      provider.dropIndex(false);
    } catch (Exception ex) {
      // ignored
    }

    try {
      provider.bootstrap();
    } catch (Exception ex) {
      if (!ex.getMessage().equals("Index already exists")) {
        throw ex;
      }
    }
  }
}
