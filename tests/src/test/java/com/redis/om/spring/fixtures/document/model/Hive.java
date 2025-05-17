package com.redis.om.spring.fixtures.document.model;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

@Document
public class Hive {

  @Id
  private UUID id;

  @Indexed
  private Set<Drone> drones;

  public Hive() {
  }

  public Hive(UUID id, Set<Drone> drones) {
    this.id = id;
    this.drones = drones;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Set<Drone> getDrones() {
    return drones;
  }

  public void setDrones(Set<Drone> drones) {
    this.drones = drones;
  }
}
