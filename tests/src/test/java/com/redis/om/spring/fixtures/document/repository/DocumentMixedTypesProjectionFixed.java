package com.redis.om.spring.fixtures.document.repository;

import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;

/**
 * Projection interface WITH @Value annotations as a workaround
 * to make non-String fields work correctly
 */
public interface DocumentMixedTypesProjectionFixed {
  
  // String fields work without @Value
  String getName();
  
  // Non-String fields need @Value annotation to work
  @Value("#{target.age}")
  Integer getAge();
  
  @Value("#{target.salary}")
  Double getSalary();
  
  @Value("#{target.active}")
  Boolean getActive();
  
  @Value("#{target.birthDate}")
  LocalDate getBirthDate();
}