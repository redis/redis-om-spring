package com.redis.om.spring.fixtures.document.model;

import java.util.UUID;

import com.redis.om.spring.annotations.Indexed;

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
