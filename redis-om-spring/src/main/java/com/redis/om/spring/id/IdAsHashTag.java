package com.redis.om.spring.id;

public class IdAsHashTag implements IdentifierFilter<Object> {
  @Override
  public String filter(Object id) {
    return "{" + id.toString() + "}";
  }
}
