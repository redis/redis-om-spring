package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Indexed;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class Employee {

  @NonNull
  @Indexed
  private String name;
}
