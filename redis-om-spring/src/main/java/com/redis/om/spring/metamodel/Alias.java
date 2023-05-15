package com.redis.om.spring.metamodel;

public class Alias<E, T> extends MetamodelField<E, T> {
  public Alias(String alias) {
    super(alias, String.class, true);
  }

  public static Alias of(String alias) {
    return new Alias<>(alias);
  }
}
