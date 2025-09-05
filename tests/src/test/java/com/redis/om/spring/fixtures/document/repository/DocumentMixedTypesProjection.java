package com.redis.om.spring.fixtures.document.repository;

import java.time.LocalDate;

/**
 * Projection interface WITHOUT @Value annotations to demonstrate the issue
 * where non-String fields return null
 */
public interface DocumentMixedTypesProjection {
  
  // String fields should work without @Value
  String getName();
  
  // Non-String fields will return null without @Value annotation
  Integer getAge();
  
  Double getSalary();
  
  Boolean getActive();
  
  LocalDate getBirthDate();
}