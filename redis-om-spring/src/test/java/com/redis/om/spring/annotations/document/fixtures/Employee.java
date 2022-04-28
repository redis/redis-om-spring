package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Indexed;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Employee {

  @NonNull
  @Indexed
  private String name;
}
