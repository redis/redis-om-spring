package com.redis.om.spring.id;

/**
 * Utility class for wrapping identifiers in hash tags for Redis cluster compatibility.
 */
public class IdAsHashTag implements IdentifierFilter<Object> {
  /**
   * Default constructor.
   */
  public IdAsHashTag() {
  }

  @Override
  public String filter(Object id) {
    return "{" + id.toString() + "}";
  }
}
