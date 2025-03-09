package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Indexed;

import java.util.UUID;

public class Drone {

  @Indexed
  private UUID content;

  public Drone() {
  }

  public Drone(UUID content) {
    this.content = content;
  }

  public UUID getContent() {
    return content;
  }

  public void setContent(UUID content) {
    this.content = content;
  }
}

