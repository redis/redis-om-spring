package com.redis.om.spring.annotations;

public enum Dialect {
  ONE(1),
  TWO(2),
  THREE(3);

  private final int value;
  Dialect(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
