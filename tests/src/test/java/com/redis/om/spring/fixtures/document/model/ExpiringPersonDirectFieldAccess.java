package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.TimeToLive;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

/**
 * Test entity that has a {@link TimeToLive} field but deliberately omits a
 * JavaBean getter for it. This reproduces the JDK 25 classloader bug where
 * {@code ObjectUtils.getGetterForField()} returns {@code null} and
 * {@code ReflectionUtils.invokeMethod(null, entity)} throws NPE.
 * <p>
 * This also simulates Groovy/Kotlin entities where property access patterns
 * may differ from standard JavaBean conventions.
 */
@Document
public class ExpiringPersonDirectFieldAccess {
  @Id
  private String id;

  @Indexed
  private String name;

  @TimeToLive
  private Long ttl;

  public ExpiringPersonDirectFieldAccess() {
  }

  public ExpiringPersonDirectFieldAccess(String name, Long ttl) {
    this.name = name;
    this.ttl = ttl;
  }

  public static ExpiringPersonDirectFieldAccess of(String name, Long ttl) {
    return new ExpiringPersonDirectFieldAccess(name, ttl);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  // Deliberately NO getter/setter for ttl field.
  // This reproduces the scenario where ObjectUtils.getGetterForField()
  // returns null, causing NPE in getTTLForEntity().
}
