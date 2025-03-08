package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Indexed;


import java.util.Objects;

public class CityDoc {
  private @Indexed String name;

  public CityDoc() {
  }

  public CityDoc(String name) {
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

    if (!(obj instanceof CityDoc that)) {
      return false;
    }

    return Objects.equals(this.getName(), that.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName());
  }
}
