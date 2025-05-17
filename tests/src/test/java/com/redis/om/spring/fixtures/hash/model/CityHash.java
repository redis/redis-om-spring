package com.redis.om.spring.fixtures.hash.model;

import java.util.Objects;

import com.redis.om.spring.annotations.Indexed;

public class CityHash {
  private @Indexed String name;

  public CityHash() {
  }

  public CityHash(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof CityHash that)) {
      return false;
    }

    return Objects.equals(this.getName(), that.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName());
  }
}
